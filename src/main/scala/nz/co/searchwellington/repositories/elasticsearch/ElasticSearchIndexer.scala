package nz.co.searchwellington.repositories.elasticsearch

import com.sksamuel.elastic4s.ElasticDsl.search
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.analyzers.StandardAnalyzer
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.HttpClient
import nz.co.searchwellington.model.{Resource, Tag}
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

@Component
class ElasticSearchIndexer @Autowired()() {

  private val log = Logger.getLogger(classOf[ElasticSearchIndexer])

  private val tenSeconds = Duration(10000, MILLISECONDS)

  private val Index = "searchwellington"
  private val Resources = "resources"

  val client = HttpClient(ElasticsearchClientUri("localhost", 9200))

  val Title = "title"
  val Type = "type"
  val HttpStatus = "http_status"
  val Description = "description"
  val Date = "date"
  val Tags = "tags"

  def updateMultipleContentItems(resources: Seq[(Resource, Set[Int])]): Unit = {
    log.info("Index batch of size: " + resources.size)

    val indexDefinations = resources.map { r =>
      val fields = Seq (
        Some((Type -> r._1.`type`)),
        r._1.title.map(t => (Title -> t)),
        Some(HttpStatus -> r._1.http_status.toString),
        r._1.description.map(d => (Description -> d)),
        r._1.date2.map(d => (Date -> new DateTime(d))),
        Some(Tags, r._2)
      )

      indexInto(Index / Resources).fields(fields.flatten) id r._1.id.toString
    }

    val result = Await.result(client.execute (bulk(indexDefinations)), tenSeconds)
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
            field(Type) typed TextType,
            field(Date) typed DateType,
            field(Description) typed TextType analyzer StandardAnalyzer,
            field(Tags) typed IntegerType
          )
          )
      }

      val result = Await.result(eventualCreateIndexResult, Duration(10, SECONDS))
      log.info("Create indexes result: " + result)

    } catch {
      case e: Exception => log.error("Failed to created index", e)
    }
  }

  def getLatestNewsitems(maxItems: Int): Future[(Seq[Int], Long)] = {
    executeRequest(ResourceQuery(`type` = Some("N")))
  }

  def getLatestWebsites(maxItems: Int): Future[(Seq[Int], Long)] = {
    executeRequest(ResourceQuery(`type` = Some("W")))
  }

  def getTagNewsitems(tag: Tag, maxItems: Int): Future[(Seq[Int], Long)] = {
    executeRequest(ResourceQuery(`type` = Some("N"), tag = Some(tag)))
  }

  private def executeRequest(query: ResourceQuery): Future[(Seq[Int], Long)] = {
    var conditions = Seq (
      query.`type`.map(t => matchQuery(Type, "N")),
      query.tag.map(t => matchQuery(Tags, t))
    ).flatten

    val q = must(conditions)

    val request = search in Index -> Resources query q sortByFieldDesc (Date) limit (query.maxItems)

    client.execute(request).map { r =>
      r.map { rs =>
        (rs.result.hits.hits.map (_.id.toInt).toSeq, rs.result.totalHits)

      } match {
        case (Right(idsWithTotalCount)) => idsWithTotalCount
        case (Left(f)) =>
          log.error(f)
          (Seq(), 0L)
      }
    }
  }

  case class ResourceQuery(`type`: Option[String] = None, tag: Option[Tag] = None, maxItems: Int = 30)

}