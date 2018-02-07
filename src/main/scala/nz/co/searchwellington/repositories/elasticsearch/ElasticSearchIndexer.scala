package nz.co.searchwellington.repositories.elasticsearch


import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.{HttpClient, RequestFailure, RequestSuccess}
import nz.co.searchwellington.model.Resource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import com.sksamuel.elastic4s.http.ElasticDsl._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, MILLISECONDS}
import scala.concurrent.{Await, Future}

@Component
class ElasticSearchIndexer @Autowired()() {

  private val Index = "searchwellington"
  private val Resources = "resources"

  val client = HttpClient(ElasticsearchClientUri("localhost", 9200))

  def updateMultipleContentItems(resources: Seq[Resource]): Unit = {
    println("Index batch of size: " + resources.size)
    resources.map { r =>
      println(r.title)

      val fields: Seq[Option[(String, Any)]] = Seq {
        r.title.map(t => ("title" -> t))
        r.description.map(d => ("description" -> d))
      }

      val x = client.execute{
        bulk(indexInto(Index / Resources).fields(fields.flatten) id r.id.toString)
      }


     val y = Await.result(x, Duration(10000, MILLISECONDS))
     println(y)
    }
  }

}
