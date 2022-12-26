package nz.co.searchwellington.filters.attributesetters;

import nz.co.searchwellington.filters.attributesetters.LocationParameterFilter.{LOCATION, RADIUS}
import nz.co.searchwellington.geocoding.osm.{GeoCodeService, OsmIdParser}
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.geo.model.{LatLong, Place}

import javax.servlet.http.HttpServletRequest
import scala.concurrent.Future;

@Component
class LocationParameterFilter @Autowired()(geoCodeService: GeoCodeService, osmIdParser: OsmIdParser) extends AttributeSetter {

  private val log = LogFactory.getLog(classOf[LocationParameterFilter])

  private val LATITUDE = "latitude"
  private val LONGITUDE = "longitude"
  private val OSM = "osm"

  override def setAttributes(request: HttpServletRequest): Future[Map[String, Any]] = {
    val maybeRadius: Option[Double] = processDoubleParameter(request, LocationParameterFilter.RADIUS).flatMap { radius =>
      if (radius > 0) {
        Some(radius)
      } else {
        None
      }
    }

    val maybeOsmPlace: Option[Place] = for {
      osmIdString <- Option(request.getParameter(OSM))
      osmId <- Option(osmIdParser.parseOsmId(osmIdString))
      resolvedPlace <- Option(geoCodeService.resolveOsmId(osmId))
    } yield {
      log.debug("OSM id '" + osmId + "' resolved to: " + resolvedPlace);
      resolvedPlace
    }

    val maybeLatLongLocation = for {
      latitide <- Option(request.getParameter(LATITUDE))
      longitude <- Option(request.getParameter(LONGITUDE))
    } yield {
      val latLong = new LatLong(latitide.toDouble, longitude.toDouble)
      val latLongLabel = latLong.getLatitude + ", " + latLong.getLongitude;
      new Place(latLongLabel, latLong, null)
    }

    Future.successful(Seq(Seq(maybeOsmPlace, maybeLatLongLocation).flatten.headOption.map { place =>
      LOCATION -> place
    }, maybeRadius.map { radius => RADIUS -> radius }).flatten.toMap)
  }

  private def processDoubleParameter(request: HttpServletRequest, parameterName: String): Option[Double] = {
    Option(request.getParameter(parameterName)).flatMap { d =>
      try {
        Some(d.toDouble)
      } catch {
        case _: NumberFormatException =>
          log.warn("User supplied invalid double " + parameterName + " value: " + request.getParameter(parameterName))
          None
      }
    }
  }
}

object LocationParameterFilter {
  val LOCATION = "location"
  val RADIUS = "radius"
}
