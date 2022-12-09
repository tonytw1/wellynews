package nz.co.searchwellington.http

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.springframework.stereotype.Component
import play.api.libs.ws.ahc.StandaloneAhcWSClient


@Component
class WSClient {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val wsClient = StandaloneAhcWSClient()

}
