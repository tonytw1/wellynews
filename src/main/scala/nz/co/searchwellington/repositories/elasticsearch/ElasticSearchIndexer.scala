package nz.co.searchwellington.repositories.elasticsearch

import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.HttpClient
import nz.co.searchwellington.model.Resource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, MILLISECONDS}

@Component
class ElasticSearchIndexer @Autowired()() {

  private val Index = "searchwellington"
  private val Resources = "resources"

  val client = HttpClient(ElasticsearchClientUri("localhost", 9200))

  def updateMultipleContentItems(resources: Seq[Resource]): Unit = {
    println("Index batch of size: " + resources.size)

    val indexDefinations = resources.map { r =>
      val fields = Seq {
        Some("type", r.`type`)
        r.title.map(t => ("title" -> t)),
        r.description.map(d => ("description" -> d)),
        Some("http_status" -> r.http_status.toString))
      }

      indexInto(Index / Resources).fields(fields.flatten) id r.id.toString
    }

    val result = Await.result(client.execute (bulk(indexDefinations)), Duration(10000, MILLISECONDS))
    println(result)
  }

}
