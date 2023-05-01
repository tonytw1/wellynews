package nz.co.searchwellington.linkchecking.cards

import akka.util.ByteString
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.http.WSClient
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.util.MimeTypeUtils
import play.api.libs.json.Json
import play.api.libs.ws.DefaultBodyWritables.writeableOf_WsBody
import play.api.libs.ws.{EmptyBody, InMemoryBody}

import scala.concurrent.{ExecutionContext, Future}

@Component
class SocialImageDetector @Autowired()(wsClient: WSClient, @Value("${cards.url}") cardsUrl: String) extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[SocialImageDetector])
  private implicit val dir = Json.reads[DetectedImage]

  def extractSocialImageUrlsFrom(pageContent: String)(implicit ec: ExecutionContext): Future[Option[Seq[DetectedImage]]] = {
    if (cardsUrl.nonEmpty) {
      val request = wsClient.wsClient.url(cardsUrl + "/detect").
        withHttpHeaders((HttpHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_HTML_VALUE)).
        withRequestTimeout(TenSeconds)

      request.post(InMemoryBody(ByteString(pageContent.getBytes))).map { r =>
        r.status match {
          case 200 =>
            Some(Json.parse(r.body).as[Seq[DetectedImage]])
          case _ =>
            log.warn(s"Cards detect call failed: ${r.status} / ${r.body})")
            None
        }
      }

    } else {
      log.warn("Cards service is not configured")
      Future.successful(None)
    }
  }

  def pin(selectedImageUrl: String)(implicit ec: ExecutionContext): Future[Boolean] = {
    val request = wsClient.wsClient.url(cardsUrl + "/pinned").
      withQueryStringParameters("url" -> selectedImageUrl).
      withRequestTimeout(ThirtySeconds)

    request.post(EmptyBody).map { r =>
      r.status match {
        case 200 =>
          true
        case _ =>
          log.warn(s"Cards pin call failed: ${r.status} / ${r.body})")
          false
      }
    }
  }
}

case class DetectedImage(url: String,
                         source: String,
                         contentType: Option[String] = None,
                         width: Option[Int] = None,
                         height: Option[Int] = None,
                         alt: Option[String] = None)