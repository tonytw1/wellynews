package nz.co.searchwellington.repositories.elasticsearch

import com.sksamuel.elastic4s.DistanceUnit
import com.sksamuel.elastic4s.analyzers.StandardAnalyzer
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.delete.DeleteResponse
import com.sksamuel.elastic4s.http.{bulk => _, delete => _, search => _, _}
import com.sksamuel.elastic4s.mappings.FieldType._
import com.sksamuel.elastic4s.searches.DateHistogramInterval
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.ShowBrokenDecisionService
import nz.co.searchwellington.model.{ArchiveLink, PublishedResource, Resource}
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import reactivemongo.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

@Component
class ElasticSearchIndexer  @Autowired()(val showBrokenDecisionService: ShowBrokenDecisionService,
                                         @Value("#{config['elasticsearch.host']}") elasticsearchHost: String,
                                         @Value("#{config['elasticsearch.port']}") elasticsearchPort: Int)
  extends ElasticFields with ModeratedQueries with ReasonableWaits {

  private val log = Logger.getLogger(classOf[ElasticSearchIndexer])

  private val Index = "searchwellington"
  private val Resources = "resources"

  val client = ElasticClient(ElasticProperties("http://" + elasticsearchHost + ":" + elasticsearchPort))

  def updateMultipleContentItems(resources: Seq[(Resource, Seq[String])]): Unit = {
    log.info("Index batch of size: " + resources.size)

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
        latLong.map(ll => LatLong -> Map("lat" -> ll.getLatitude, "lon" -> ll.getLongitude))
      )

      indexInto(Index / Resources).fields(fields.flatten) id r._1._id.stringify
    }

    Await.result(client.execute (bulk(indexDefinitions)), TenSeconds)
  }

  def deleteResource(id: BSONObjectID): Future[Response[DeleteResponse]] = {
    client execute(
      delete(id.stringify) from Index / Resources
    )
  }

  def createIndexes() = {

    try {
      val eventualCreateIndexResult = client.execute {
        createIndex(Index) mappings (
          mapping(Resources).fields(
            field(Title) typed TextType analyzer StandardAnalyzer,
            field(Type) typed KeywordType,
            field(Date) typed DateType,
            field(Description) typed TextType analyzer StandardAnalyzer,
            field(Tags) typed KeywordType,
            field(Publisher) typed KeywordType,
            field(Held) typed BooleanType,
            field(Owner) typed KeywordType,
            field(LatLong) typed GeoPointType
          )
          )
      }

      val result = Await.result(eventualCreateIndexResult, Duration(10, SECONDS))
      log.info("Create indexes result: " + result)

    } catch {
      case e: Exception => log.error("Failed to created index", e)
    }
  }

  def getResources(query: ResourceQuery): Future[(Seq[BSONObjectID], Long)] = executeResourceQuery(query)

  def getAllPublishers(): Future[Seq[String]] = {
    val allNewsitems = matchQuery(Type, "N")
    val aggs = Seq(termsAgg("publisher", "publisher") size Integer.MAX_VALUE)
    val request = (search(Index / Resources) query allNewsitems) limit 0 aggregations (aggs)
    client.execute(request).map { r =>
      r.result.aggregations.terms("publisher").buckets.map(b => b.key)
    }
  }

  def getArchiveMonths: Future[Seq[ArchiveLink]] = {
    val allNewsitems = matchQuery(Type, "N")
    val aggs = Seq(dateHistogramAgg("date", "date").interval(DateHistogramInterval.Month))
    val request = search(Index / Resources) query withModeration(allNewsitems) limit 0 aggregations (aggs)

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

  private def executeResourceQuery(query: ResourceQuery): Future[(Seq[BSONObjectID], Long)] = {
    var conditions = Seq(
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
      }
    ).flatten

    val q = must(conditions)

    val request = search(Index / Resources) query withModeration(q) sortByFieldDesc Date start query.startIndex limit query.maxItems

    client.execute(request).map { r =>
      val hits = r.result.hits.hits
      val ids = hits.map(h => BSONObjectID(h.id))
      val total = r.result.totalHits
      (ids, total)
    }
  }

}
