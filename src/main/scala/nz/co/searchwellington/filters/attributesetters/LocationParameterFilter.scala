package nz.co.searchwellington.filters.attributesetters;

import com.google.common.base.Strings
import nz.co.searchwellington.exceptions.UnresolvableLocationException
import nz.co.searchwellington.geocoding.osm.{GeoCodeService, OsmIdParser}
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.geo.model.{LatLong, Place}

import javax.servlet.http.HttpServletRequest
import scala.concurrent.Future;

@Component
@Scope(value = "request")
class LocationParameterFilter @Autowired()(geoCodeService: GeoCodeService, osmIdParser: OsmIdParser) extends AttributeSetter {

  private val log = LogFactory.getLog(classOf[LocationParameterFilter])

  private val LATITUDE = "latitude"
  private val LONGITUDE = "longitude"
  private val OSM = "osm"

  override def setAttributes(request: HttpServletRequest): Future[Boolean] = {
    processDoubleParameter(request, LocationParameterFilter.RADIUS).foreach { radius =>
      if (radius > 0) {
        request.setAttribute(LocationParameterFilter.RADIUS, radius);
      }
    }

    if (!Strings.isNullOrEmpty(request.getParameter(OSM))) {
      val osmIdString = request.getParameter(OSM);
      val osmId = osmIdParser.parseOsmId(osmIdString)
      if (osmId != null) {
        val resolvedPlace = geoCodeService.resolveOsmId(osmId);
        log.debug("OSM id '" + osmId + "' resolved to: " + resolvedPlace);
        if (resolvedPlace == null) {
          throw new UnresolvableLocationException("OSM place could not be resolved");
        }
        request.setAttribute(LocationParameterFilter.LOCATION, resolvedPlace)
      }
      Future.successful(false)

    } else if (!Strings.isNullOrEmpty(request.getParameter("latitude")) && !Strings.isNullOrEmpty(request.getParameter("longitude"))) {
      val latLong = new LatLong(request.getParameter(LATITUDE).toDouble, request.getParameter(LONGITUDE).toDouble)
      val latLongLabel = latLong.getLatitude() + ", " + latLong.getLongitude();
      // TODO - is you wanted to, you could resolve for a name, but don't alter the user supplied lat/long values.
      // TODO lat, long isn't really an address - this should be something like a display method on latLong or the view which gives a sensible output when address is null.
      request.setAttribute(LocationParameterFilter.LOCATION, new Place(latLongLabel, latLong, null));
      Future.successful(false)

    } else {
      Future.successful(false)
    }
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
