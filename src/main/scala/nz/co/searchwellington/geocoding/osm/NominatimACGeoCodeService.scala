package nz.co.searchwellington.geocoding.osm

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.http.WSClient
import org.apache.commons.logging.LogFactory
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import play.api.libs.json.{Json, Reads}
import uk.co.eelpieconsulting.common.geo.model.{LatLong, OsmId, OsmType, Place}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Component
class NominatimACGeoCodeService @Autowired()(wsClient: WSClient) extends GeoCodeService with ReasonableWaits {

  private val log = LogFactory.getLog(classOf[NominatimACGeoCodeService])

  override def resolveOsmId(osmId: OsmId): Place = {
    val url = "https://nominatim-ac.eelpieconsulting.co.uk/places/" + osmId.getId + osmId.getType.toString.take(1)

    val eventualPlace = wsClient.wsClient.url(url).
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

    Await.result(eventualPlace, TenSeconds) // TODO push up
  }

  private case class NominatimACPlace(address: String, osmId: Long, osmType: String, latlong: Option[NominatimACLatLong])

  case class NominatimACLatLong(lat: Double, lon: Double)

}
