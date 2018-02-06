package nz.co.searchwellington.repositories.elasticsearch

import com.sksamuel.elastic4s.{ElasticsearchClientUri, TcpClient}
import nz.co.searchwellington.model.Resource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ElasticSearchIndexer @Autowired()() {

  private val Index = "searchwellington"
  private val Resources = "resources"

  val client = TcpClient.transport(ElasticsearchClientUri("localhost", 9300))

  def updateMultipleContentItems(resources: Seq[Resource]): Unit = {
    resources.map { r =>
      println(r.title)
    }
  }

}
