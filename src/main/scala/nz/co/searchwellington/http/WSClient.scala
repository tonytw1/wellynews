package nz.co.searchwellington.http

import akka.actor.ActorSystem
import akka.stream.{Materializer, SystemMaterializer}
import org.springframework.stereotype.Component
import play.api.libs.ws.ahc.StandaloneAhcWSClient

@Component
class WSClient {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = SystemMaterializer(system).materializer

  val wsClient: StandaloneAhcWSClient = StandaloneAhcWSClient()

}
