package nz.co.searchwellington.geocoding.osm

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import nz.co.searchwellington.ReasonableWaits
import org.apache.http.HttpStatus
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import play.api.libs.json.{Json, Reads}
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import uk.co.eelpieconsulting.common.geo.model.{LatLong, OsmId, OsmType, Place}

import scala.concurrent.{Await, ExecutionContext}

@Component
class NominatimACGeoCodeService @Autowired()() extends GeoCodeService with ReasonableWaits {

  private val log = Logger.getLogger(classOf[NominatimACGeoCodeService])

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  private val wsClient = StandaloneAhcWSClient()

  private implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  override def resolveOsmId(osmId: OsmId): Place = {
    val url = "https://nominatim-ac.eelpieconsulting.co.uk/places/" + osmId.getId + osmId.getType.toString.take(1)

    val eventualPlace = wsClient.url(url).
      withRequestTimeout(TenSeconds).get.map { result =>
        result.status match {
          case HttpStatus.SC_OK =>
            implicit val nacll: Reads[NominatimACLatLong] = Json.reads[NominatimACLatLong]
            implicit val nacpr: Reads[NominatimACPlace] = Json.reads[NominatimACPlace]

            val body = result.body
            log.info("Nominatim AC response: " + body)

            val nominatimACPlace = Json.parse(body).as[NominatimACPlace]
            log.info("Nominatim AC response parsed: " + nominatimACPlace)

            val osmType = Option(nominatimACPlace.osmType).flatMap { osmType =>
              osmType match {
                case "N" => Some(OsmType.NODE)
                case "W" => Some(OsmType.WAY)
                case "R" => Some(OsmType.RELATION)
                case _ => None
              }
            }

            val osmId = for {
              i <- Option(nominatimACPlace.osmId)
              t <- osmType
            } yield {
              new OsmId(i, t)
            }

            val latLong = nominatimACPlace.latlong.map { ll =>
              new LatLong(ll.lat, ll.lon)
            }

            val place = new Place(
              nominatimACPlace.address,
              latLong.orNull,
              osmId.orNull
            )

            log.info("Place: " + place)
            place

          case _ =>
            log.warn("NominatimAC call to " + url + " failed with status: " + result.status)
            null
        }
    }

    Await.result(eventualPlace, TenSeconds)
  }

  case class NominatimACPlace(address: String, osmId: Long, osmType: String, latlong: Option[NominatimACLatLong])
  case class NominatimACLatLong (lat: Double, lon: Double)

}
