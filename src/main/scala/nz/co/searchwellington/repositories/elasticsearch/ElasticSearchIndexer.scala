package nz.co.searchwellington.repositories.elasticsearch

import com.sksamuel.elastic4s.ElasticDsl.search
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.analyzers.StandardAnalyzer
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.index.CreateIndexResponse
import com.sksamuel.elastic4s.http.{HttpClient, RequestFailure, RequestSuccess}
import com.sksamuel.elastic4s.searches._
import nz.co.searchwellington.model.Resource
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, MILLISECONDS}
import scala.concurrent.{Await, Future}

@Component
class ElasticSearchIndexer @Autowired()() {

  private val tenSeconds = Duration(10000, MILLISECONDS)

  private val Index = "searchwellington"
  private val Resources = "resources"

  val client = HttpClient(ElasticsearchClientUri("localhost", 9200))

  val Title = "title"
  val Type = "type"
  val HttpStatus = "http_status"
  val Description = "description"
  val Date = "date"

  def updateMultipleContentItems(resources: Seq[Resource]): Unit = {
    println("Index batch of size: " + resources.size)

    val indexDefinations = resources.map { r =>
      println("! " + r.date2)
      val fields = Seq (
        Some((Type -> r.`type`)),
        r.title.map(t => (Title -> t)),
        Some(HttpStatus -> r.http_status.toString),
        r.description.map(d => (Description -> d)),
        r.date2.map(d => (Date -> new DateTime(d)))
      )

      indexInto(Index / Resources).fields(fields.flatten) id r.id.toString
    }

    val result = Await.result(client.execute (bulk(indexDefinations)), tenSeconds)
    println(result)
  }

  def readBack() = {
    var request: SearchDefinition = {
      search in Index / Resources matchQuery(Type, "L")
    }

    val result = Await.result({client.execute (request)}, tenSeconds)
    result.map { r =>
      println("!!!!!! " + r.result.hits.total)
      r.result.hits.hits.map { h =>
        println("!!!!! " + h)
      }
    }
  }

  def createIndexes() = {
    import com.sksamuel.elastic4s.ElasticDsl._
    import com.sksamuel.elastic4s.mappings.FieldType._

    try {
      val x: Future[Either[RequestFailure, RequestSuccess[CreateIndexResponse]]] = client.execute {
        create index Index mappings (
          mapping(Resources).fields(
            field(Title) typed TextType analyzer StandardAnalyzer,
            field(Type) typed TextType,
            field(Date) typed DateType,
            field(Description) typed TextType analyzer StandardAnalyzer
          )
          )
      }

      val result = x.await
      println("!!! " + result)

    } catch {
      case e: Exception => println("Failed to created index", e)
    }

    println("Index created")
  }

  def getLatestNewsitems(maxItems: Int): Seq[Int] = {
    executeRequest(search in Index / Resources matchQuery(Type, "N") sortByFieldDesc(Date) limit (maxItems) )
  }

  def getLatestWebsites(maxItems: Int): Seq[Int] = {
    executeRequest(search in Index / Resources matchQuery(Type, "W") sortByFieldDesc(Date) limit (maxItems) )
  }

  private def executeRequest(request: SearchDefinition) = {
    Await.result(client.execute(request), tenSeconds).map { r =>
      r.result.hits.hits.map { h =>
        h.id.toInt
      }.toSeq

    } match {
      case(Right(ids)) => ids
      case(Left(f)) =>
        println(f)
        Seq()
    }
  }

}
