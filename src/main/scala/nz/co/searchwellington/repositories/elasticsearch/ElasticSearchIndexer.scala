package nz.co.searchwellington.repositories.elasticsearch

import com.sksamuel.elastic4s.ElasticDsl.search
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.analyzers.StandardAnalyzer
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.HttpClient
import com.sksamuel.elastic4s.http.search.TermsAggResult
import com.sksamuel.elastic4s.searches.DateHistogramInterval
import nz.co.searchwellington.ReasonableWaits
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
class ElasticSearchIndexer  @Autowired()(@Value("#{config['elasticsearch.host']}") elasticsearchHost: String,
                                         @Value("#{config['elasticsearch.port']}") elasticsearchPort: Int) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[ElasticSearchIndexer])

  private val Index = "searchwellington"
  private val Resources = "resources"

  val client = HttpClient(ElasticsearchClientUri(elasticsearchHost, elasticsearchPort))

  val Title = "title"
  val Type = "type"
  val HttpStatus = "http_status"
  val Description = "description"
  val Date = "date"
  val Tags = "tags"
  val Publisher = "publisher"
  val Held = "held"

  def updateMultipleContentItems(resources: Seq[(Resource, Set[String])]): Unit = {
    log.info("Index batch of size: " + resources.size)

    val indexDefinitions = resources.map { r =>
      val publisher = r._1 match {
        case p: PublishedResource => p.getPublisher
        case _ => None
      }

      // TODO This is silly; just pass in the whole domain object as JSON
      val fields = Seq (
        Some(Type -> r._1.`type`),
        r._1.title.map(t => Title -> t),
        Some(HttpStatus -> r._1.http_status.toString),
        r._1.description.map(d => Description -> d),
        r._1.date.map(d => Date -> new DateTime(d)),
        Some(Tags, r._2),
        publisher.map(p => Publisher -> p.stringify),
        Some(Held -> r._1.held)
      )

      indexInto(Index / Resources).fields(fields.flatten) id r._1._id.stringify
    }

    val result = Await.result(client.execute (bulk(indexDefinitions)), TenSeconds)
    log.info(result)
  }

  def createIndexes() = {
    import com.sksamuel.elastic4s.ElasticDsl._
    import com.sksamuel.elastic4s.mappings.FieldType._

    try {
      val eventualCreateIndexResult = client.execute {
        create index Index mappings (
          mapping(Resources).fields(
            field(Title) typed TextType analyzer StandardAnalyzer,
            field(Type) typed KeywordType,
            field(Date) typed DateType,
            field(Description) typed TextType analyzer StandardAnalyzer,
            field(Tags) typed KeywordType,
            field(Publisher) typed KeywordType,
            field(Held) typed BooleanType
          )
          )
      }

      val result = Await.result(eventualCreateIndexResult, Duration(10, SECONDS))
      log.info("Create indexes result: " + result)

    } catch {
      case e: Exception => log.error("Failed to created index", e)
    }
  }

  def getResources(query: ResourceQuery): Future[(Seq[BSONObjectID], Long)] = executeRequest(query)

  def getAllPublishers(): Future[Seq[String]] = {
    val allNewsitems = matchQuery(Type, "N")

    val aggs = Seq(termsAgg("publisher", "publisher") size Integer.MAX_VALUE)

    val request = (search in Index / Resources query allNewsitems) limit 0 aggregations (aggs)

    client.execute(request).map { r =>

      val resultBuckets = r.map { rs =>
        val publisherAgg: TermsAggResult = rs.result.aggregations.terms("publisher")
        publisherAgg.buckets.map(b => b.key)
      }

      resultBuckets match {
        case Right(buckets) => {
          buckets
        }
        case Left(f) =>
          log.error(f)
          Seq()
      }
    }
  }

  def getArchiveMonths(shouldShowBroken: Boolean): Future[Seq[ArchiveLink]] = {
    val newsitems = matchQuery(Type, "N")
    val aggs = Seq(dateHistogramAgg("date", "date").interval(DateHistogramInterval.Month))

    val request = (search in Index / Resources query newsitems) limit 0 aggregations (aggs)

    client.execute(request).map { r =>
      val resultBuckets = r.map { rs =>
        val dateAgg = rs.result.aggregations.dateHistogram("date")
        dateAgg.buckets
      }
      resultBuckets match {
        case Right(buckets) => {
          buckets.map { b =>
            new ArchiveLink(ISODateTimeFormat.dateTimeParser().parseDateTime(b.date).toDate, b.docCount)
          }
        }
        case Left(f) =>
          log.error(f)
          Seq()
      }
    }.map(_.filter(_.getCount > 0).reverse) // TODO can do this in elastic query?
  }

  private def executeRequest(query: ResourceQuery): Future[(Seq[BSONObjectID], Long)] = {
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
      }
    ).flatten

    val withModerationConditions = conditions :+ matchQuery(Held, false)

    val q = must(withModerationConditions)

    val request = search in Index -> Resources query q sortByFieldDesc Date limit query.maxItems

    client.execute(request).map { r =>
      r.map { rs =>
        (rs.result.hits.hits.map(h => BSONObjectID(h.id)).toSeq, rs.result.totalHits)
      } match {
        case Right(idsWithTotalCount) => {
          log.info(query + ": " + idsWithTotalCount._2)
          idsWithTotalCount
        }
        case Left(f) =>
          log.error(f)
          (Seq(), 0L)
      }
    }
  }

}
