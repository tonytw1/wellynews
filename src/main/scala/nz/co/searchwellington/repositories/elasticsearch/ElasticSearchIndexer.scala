package nz.co.searchwellington.repositories.elasticsearch

import com.sksamuel.elastic4s.DistanceUnit
import com.sksamuel.elastic4s.analyzers.StandardAnalyzer
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.bulk.BulkResponse
import com.sksamuel.elastic4s.http.delete.DeleteResponse
import com.sksamuel.elastic4s.http.{bulk => _, delete => _, search => _, _}
import com.sksamuel.elastic4s.indexes.IndexRequest
import com.sksamuel.elastic4s.searches.queries.Query
import com.sksamuel.elastic4s.searches.{DateHistogramInterval, SearchRequest}
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.ShowBrokenDecisionService
import nz.co.searchwellington.model._
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import org.apache.log4j.Logger
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{DateTime, Interval}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import reactivemongo.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

@Component
class ElasticSearchIndexer @Autowired()(val showBrokenDecisionService: ShowBrokenDecisionService,
                                        @Value("#{config['elasticsearch.host']}") elasticsearchHost: String,
                                        @Value("#{config['elasticsearch.port']}") elasticsearchPort: Int,
                                        @Value("#{config['elasticsearch.index']}") elasticsearchIndex: String,
                                        taggingReturnsOfficerService: TaggingReturnsOfficerService)
  extends ElasticFields with ModeratedQueries with ReasonableWaits {

  def byDateDescending(request: SearchRequest): SearchRequest = request sortByFieldDesc Date

  def byTitleAscending(request: SearchRequest): SearchRequest = request sortByFieldAsc TitleSort

  def byLastChangedDescending(request: SearchRequest): SearchRequest = request sortByFieldDesc LastChanged

  def byFeedLatestFeedItemDate(request: SearchRequest): SearchRequest = request sortByFieldDesc FeedLatestItemDate

  private val log = Logger.getLogger(classOf[ElasticSearchIndexer])

  private val Index = elasticsearchIndex
  private val Resources = "resources"

  private val client = {
    def ensureIndexes(client: ElasticClient): Unit = {
      val exists = Await.result((client execute indexExists(Index)).map { r =>
        if (r.isSuccess) {
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

    def createIndexes(client: ElasticClient): Unit = {
      try {
        val eventualCreateIndexResult = client.execute {
          createIndex(Index) mappings mapping(Resources).fields(
            textField(Title) analyzer StandardAnalyzer,
            keywordField(TitleSort),
            keywordField(Type),
            dateField(Date),
            textField(Description) analyzer StandardAnalyzer,
            keywordField(Tags),
            keywordField(TaggingUsers),
            keywordField(Publisher),
            booleanField(Held),
            keywordField(Owner),
            geopointField(LatLong),
            keywordField(FeedAcceptancePolicy),
            dateField(FeedLatestItemDate),
            dateField(LastChanged)
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

    val eventualIndexDefinitions: Seq[Future[IndexRequest]] = resources.map { r =>
      val publisher = r._1 match {
        case p: PublishedResource => p.getPublisher
        case _ => None
      }

      val eventualGeotagVotes = taggingReturnsOfficerService.getGeotagVotesForResource(r._1)
      eventualGeotagVotes.map { geotagVotes =>
        val indexedGeocode = geotagVotes.headOption.map(_.geocode)
        val latLong = indexedGeocode.flatMap { gc =>
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
        val feedLatestItemDate = r._1 match {
          case f: Feed => f.latestItemDate
          case _ => None
        }


        // TODO This is silly; just pass in the whole domain object as JSON
        val fields = Seq(
          Some(Type -> r._1.`type`),
          r._1.title.map(t => Title -> t),
          r._1.title.map(t => TitleSort -> t),
          Some(HttpStatus -> r._1.http_status.toString),
          r._1.description.map(d => Description -> d),
          r._1.date.map(d => Date -> new DateTime(d)),
          Some(Tags, r._2),
          publisher.map(p => Publisher -> p.stringify),
          Some(Held -> r._1.held),
          r._1.owner.map(o => Owner -> o.stringify),
          latLong.map(ll => LatLong -> Map("lat" -> ll.getLatitude, "lon" -> ll.getLongitude)),
          Some(TaggingUsers, r._1.resource_tags.map(_.user_id.stringify)),
          feedAcceptancePolicy.map(ap => FeedAcceptancePolicy -> ap.toString),
          feedLatestItemDate.map(fid => FeedLatestItemDate -> new DateTime(fid)),
          r._1.last_changed.map(lc => LastChanged -> new DateTime(lc))
        )

        indexInto(Index / Resources).fields(fields.flatten) id r._1._id.stringify
      }
    }

    Future.sequence(eventualIndexDefinitions).flatMap { indexDefinitions =>
      client.execute(bulk(indexDefinitions))
    }
  }

  def deleteResource(id: BSONObjectID): Future[Response[DeleteResponse]] = {
    client execute (
      delete(id.stringify) from Index / Resources
      )
  }

  def getResources(query: ResourceQuery, order: SearchRequest => SearchRequest = byDateDescending, loggedInUser: Option[User]): Future[(Seq[BSONObjectID], Long)] = {
    executeResourceQuery(query, order, loggedInUser)
  }

  def getPublisherAggregationFor(query: ResourceQuery, loggedInUser: Option[User]): Future[Seq[(String, Long)]] = {
    getAggregationFor(query, Publisher, loggedInUser)
  }

  def getAggregationFor(query: ResourceQuery, aggName: String, loggedInUser: Option[User]): Future[Seq[(String, Long)]] = {
    val aggs = Seq(termsAgg(aggName, aggName) size Integer.MAX_VALUE)
    val request = (search(Index / Resources) query composeQueryFor(query, loggedInUser)) limit 0 aggregations aggs
    client.execute(request).map { r =>
      r.result.aggregations.terms(aggName).buckets.map(b => (b.key, b.docCount))
    }
  }

  def getArchiveCounts(loggedInUser: Option[User]): Future[Map[String, Long]] = {
    val everyThing = matchAllQuery
    val aggs = Seq(termsAgg("type", "type"))
    val request = search(Index / Resources) query withModeration(everyThing, loggedInUser) limit 0 aggregations aggs

    client.execute(request).map { r =>
      val typeAgg = r.result.aggregations.terms("type")
      typeAgg.buckets.map { b =>
        (b.key, b.docCount)
      }.toMap
    }
  }

  private def executeResourceQuery(query: ResourceQuery, order: SearchRequest => SearchRequest, loggedInUser: Option[User]): Future[(Seq[BSONObjectID], Long)] = {
    val request = order(search(Index / Resources) query composeQueryFor(query, loggedInUser)) start query.startIndex limit query.maxItems

    val start = DateTime.now()
    val eventualTuple: Future[(Seq[BSONObjectID], Long)] = client.execute(request).map { r =>
      val hits = r.result.hits.hits
      val ids = hits.map(h => BSONObjectID.parse(h.id).get)
      val total = r.result.totalHits
      (ids, total)
    }

    val x: Future[(Seq[BSONObjectID], Long)] = eventualTuple.map { r =>
      val duration = new org.joda.time.Duration(start, DateTime.now)
      log.info("Elastic query " + query + " took: " + duration.getMillis + " ms")
      r
    }
    x
  }

  private def composeQueryFor(query: ResourceQuery, loggedInUser: Option[User]): Query = {
    val conditions = Seq(
      query.`type`.map { `type` =>
        should {
          `type`.map { t =>
            matchQuery(Type, t)
          }
        }
      },
      query.tags.map { tags =>
        must {
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
      },
      query.held.map { h =>
        matchQuery(Held, h)
      }
    ).flatten

    withModeration(must(conditions), loggedInUser)
  }

  def createdMonthAggregationFor(query: ResourceQuery, loggedInUser: Option[User]): Future[Seq[(Interval, Long)]] = {
    val aggs = Seq(dateHistogramAgg("date", "date").interval(DateHistogramInterval.Month))
    val request = search(Index / Resources) query composeQueryFor(query, loggedInUser) limit 0 aggregations aggs

    client.execute(request).map { r =>
      val dateAgg = r.result.aggregations.dateHistogram("date")
      val archiveLinks = dateAgg.buckets.map { b =>
        val startOfMonth = ISODateTimeFormat.dateTimeParser().parseDateTime(b.date)
        val month = new Interval(startOfMonth, startOfMonth.plusMonths(1))
        (month, b.docCount)
      }
      archiveLinks.filter(_._2 > 0).reverse
    }
  }

}
