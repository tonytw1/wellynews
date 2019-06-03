package nz.co.searchwellington.repositories.elasticsearch

import com.sksamuel.elastic4s.DistanceUnit
import com.sksamuel.elastic4s.analyzers.StandardAnalyzer
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.bulk.BulkResponse
import com.sksamuel.elastic4s.http.delete.DeleteResponse
import com.sksamuel.elastic4s.http.index.admin.IndexExistsResponse
import com.sksamuel.elastic4s.http.{bulk => _, delete => _, search => _, _}
import com.sksamuel.elastic4s.mappings.FieldType._
import com.sksamuel.elastic4s.searches.queries.Query
import com.sksamuel.elastic4s.searches.{DateHistogramInterval, SearchRequest}
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.ShowBrokenDecisionService
import nz.co.searchwellington.model._
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import reactivemongo.bson.BSONObjectID
import uk.co.eelpieconsulting.common.geo.model.LatLong

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

@Component
class ElasticSearchIndexer @Autowired()(val showBrokenDecisionService: ShowBrokenDecisionService,
                                        @Value("#{config['elasticsearch.host']}") elasticsearchHost: String,
                                        @Value("#{config['elasticsearch.port']}") elasticsearchPort: Int)
  extends ElasticFields with ModeratedQueries with ReasonableWaits {

  private val log = Logger.getLogger(classOf[ElasticSearchIndexer])

  private val Index = "searchwellington"
  private val Resources = "resources"

  val client = {

    def ensureIndexes(client: ElasticClient): Unit = {
      val exists = Await.result((client execute indexExists(Index)).map { r =>
        val a: Response[IndexExistsResponse] = r
        if (a.isSuccess) {
          r.result.exists
        } else {
          throw new RuntimeException("Could not determine if index exists")
        }
      }, TenSeconds)

      if (!exists) {
        log.info("Index does not exist; creating")
        createIndexes(client)
      }
    }

    def createIndexes(client: ElasticClient) = {
      try {
        val eventualCreateIndexResult = client.execute {
          createIndex(Index) mappings mapping(Resources).fields(
            field(Title) typed TextType analyzer StandardAnalyzer fielddata true,
            field(Type) typed KeywordType,
            field(Date) typed DateType,
            field(Description) typed TextType analyzer StandardAnalyzer,
            field(Tags) typed KeywordType,
            field(TaggingUsers) typed KeywordType,
            field(Publisher) typed KeywordType,
            field(Held) typed BooleanType,
            field(Owner) typed KeywordType,
            field(LatLong) typed GeoPointType,
            field(FeedAcceptancePolicy) typed KeywordType
          )
        }

        val result = Await.result(eventualCreateIndexResult, Duration(10, SECONDS))
        log.info("Create indexes result: " + result)

      } catch {
        case e: Exception => log.error("Failed to created index", e)
      }
    }

    val client = ElasticClient(ElasticProperties("http://" + elasticsearchHost + ":" + elasticsearchPort))
    ensureIndexes(client)
    client
  }

  def updateMultipleContentItems(resources: Seq[(Resource, Seq[String])]): Future[Response[BulkResponse]] = {
    log.debug("Index batch of size: " + resources.size)

    val indexDefinitions = resources.map { r =>
      val publisher = r._1 match {
        case p: PublishedResource => p.getPublisher
        case _ => None
      }

      val latLong = r._1.geocode.flatMap { gc =>
        gc.latitude.flatMap { lat =>
          gc.longitude.map { lon =>
            new uk.co.eelpieconsulting.common.geo.model.LatLong(lat, lon)
          }
        }
      }

      val feedAcceptancePolicy = r._1 match {
        case f: Feed => Some(f.acceptance)
        case _ => None
      }

      // TODO This is silly; just pass in the whole domain object as JSON
      val fields = Seq(
        Some(Type -> r._1.`type`),
        r._1.title.map(t => Title -> t),
        Some(HttpStatus -> r._1.http_status.toString),
        r._1.description.map(d => Description -> d),
        r._1.date.map(d => Date -> new DateTime(d)),
        Some(Tags, r._2),
        publisher.map(p => Publisher -> p.stringify),
        Some(Held -> r._1.held),
        r._1.owner.map(o => Owner -> o.stringify),
        latLong.map(ll => LatLong -> Map("lat" -> ll.getLatitude, "lon" -> ll.getLongitude)),
        Some(TaggingUsers, r._1.resource_tags.map(_.user_id.stringify)),
        feedAcceptancePolicy.map(ap => FeedAcceptancePolicy -> ap.toString)
      )

      log.info(fields.flatten)

      indexInto(Index / Resources).fields(fields.flatten) id r._1._id.stringify
    }

    client.execute(bulk(indexDefinitions))
  }

  def deleteResource(id: BSONObjectID): Future[Response[DeleteResponse]] = {
    client execute (
      delete(id.stringify) from Index / Resources
      )
  }

  def getResources(query: ResourceQuery, order: SearchRequest => SearchRequest = byDateDescending): Future[(Seq[BSONObjectID], Long)] = executeResourceQuery(query, order)

  def getAllPublishers: Future[Seq[(String, Long)]] = {
    val allNewsitems = ResourceQuery(`type` = Some("N"))
    getPublisherAggregationFor(allNewsitems)
  }

  def getPublishersForTag(tag: Tag): Future[Seq[(String, Long)]] = {
    val newsitemsForTag = ResourceQuery(`type` = Some("N"), tags = Some(Set(tag)))
    getPublisherAggregationFor(newsitemsForTag)
  }

  def getPublishersNear(latLong: LatLong, radius: Double): Future[Seq[(String, Long)]] = {
    getPublisherAggregationFor(nearbyNewsitemsQuery(latLong, radius))
  }

  def getTagsNear(latLong: LatLong, radius: Double): Future[Seq[(String, Long)]] = {
    getAggregationFor(nearbyNewsitemsQuery(latLong, radius), Tags)
  }

  def getTagAggregation(tag: Tag): Future[Seq[(String, Long)]] = {
    val newsitemsForTag = ResourceQuery(`type` = Some("N"), tags = Some(Set(tag)))
    getAggregationFor(newsitemsForTag, Tags)
  }

  private def getPublisherAggregationFor(query: ResourceQuery): Future[Seq[(String, Long)]] = {
    getAggregationFor(query, Publisher)
  }

  private def getAggregationFor(query: ResourceQuery, aggName: String): Future[Seq[(String, Long)]] = {
    val aggs = Seq(termsAgg(aggName, aggName) size Integer.MAX_VALUE)
    val request = (search(Index / Resources) query composeQueryFor(query)) limit 0 aggregations (aggs)
    client.execute(request).map { r =>
      r.result.aggregations.terms(aggName).buckets.map(b => (b.key, b.docCount))
    }
  }

  def getArchiveMonths: Future[Seq[ArchiveLink]] = {
    val aggs = Seq(dateHistogramAgg("date", "date").interval(DateHistogramInterval.Month))
    val request = search(Index / Resources) query composeQueryFor(allNewsitems) limit 0 aggregations (aggs)

    client.execute(request).map { r =>
      val dateAgg = r.result.aggregations.dateHistogram("date")
      val archiveLinks = dateAgg.buckets.map { b =>
        new ArchiveLink(ISODateTimeFormat.dateTimeParser().parseDateTime(b.date).toDate, b.docCount)
      }
      archiveLinks.filter(_.getCount > 0).reverse
    }
  }

  def getArchiveCounts: Future[Map[String, Long]] = {
    val everyThing = matchAllQuery
    val aggs = Seq(termsAgg("type", "type"))
    val request = search(Index / Resources) query withModeration(everyThing) limit 0 aggregations (aggs)

    client.execute(request).map { r =>
      val typeAgg = r.result.aggregations.terms("type")
      typeAgg.buckets.map { b =>
        (b.key, b.docCount)
      }.toMap
    }
  }

  def byDateDescending(request: SearchRequest): SearchRequest = request sortByFieldDesc Date

  def byTitleAscending(request: SearchRequest): SearchRequest = request sortByFieldAsc Title

  private def executeResourceQuery(query: ResourceQuery, order: SearchRequest => SearchRequest): Future[(Seq[BSONObjectID], Long)] = {
    val request = order(search(Index / Resources) query composeQueryFor(query)) start query.startIndex limit query.maxItems

    client.execute(request).map { r =>
      val hits = r.result.hits.hits
      val ids = hits.map(h => BSONObjectID(h.id))
      val total = r.result.totalHits
      (ids, total)
    }
  }

  private def composeQueryFor(query: ResourceQuery): Query = {
    val conditions = Seq(
      query.`type`.map(t => matchQuery(Type, t)),
      query.tags.map { tags =>
        should { // TODO AND or OR
          tags.map { t =>
            matchQuery(Tags, t._id.stringify)
          }
        }
      },
      query.publisher.map { p =>
        matchQuery(Publisher, p._id.stringify)
      },
      query.interval.map { i =>
        rangeQuery("date") gte i.getStartMillis lt i.getEndMillis
      },
      query.q.map { qt =>
        val titleMatches = matchQuery(Title, qt).boost(5)
        val descriptionMatches = matchQuery(Description, qt)
        should(titleMatches, descriptionMatches)
      },
      query.owner.map { o =>
        termQuery(Owner, o.stringify)
      },
      query.geocoded.map { g =>
        if (g) {
          existsQuery(LatLong)
        } else {
          not(existsQuery(LatLong))
        }
      },
      query.circle.map { c =>
        geoDistanceQuery(LatLong).point(c.centre.getLatitude, c.centre.getLongitude).distance(c.radius, DistanceUnit.KILOMETERS)
      },
      query.taggingUser.map { tu =>
        matchQuery(TaggingUsers, tu.stringify)
      },
      query.feedAcceptancePolicy.map { ap =>
        matchQuery(FeedAcceptancePolicy, ap.toString)
      }
    ).flatten

    withModeration(must(conditions))
  }

  private val allNewsitems = ResourceQuery(`type` = Some("N"))

  private def nearbyNewsitemsQuery(latLong: LatLong, radius: Double) = ResourceQuery(`type` = Some("N"), circle = Some(Circle(latLong, radius)))

}
