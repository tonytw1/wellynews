package nz.co.searchwellington.views

import nz.co.searchwellington.model.Newsitem
import nz.co.searchwellington.model.geo.Geocode
import org.apache.commons.logging.LogFactory
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.geo.DistanceMeasuringService
import uk.co.eelpieconsulting.common.geo.model.LatLong

@Component class MapPinDeduplicator() {

  private val ONE_HUNDRED_METERS = 0.1
  private val log = LogFactory.getLog(classOf[MapPinDeduplicator])

  private val distanceMeasuringService = new DistanceMeasuringService

  def dedupe(geocoded: Seq[Newsitem], selected: Option[Newsitem] = None): Seq[Newsitem] = {
    log.debug("Deduping collection with " + geocoded.size + " items")
    var deduped = Seq(selected).flatten

    geocoded.foreach { resource =>
      resource.geocode.foreach { p =>
        val isUnique = !listAlreadyContainsResourceWithThisLocation(deduped, p)
        if (isUnique) deduped = deduped :+ resource
      }
    }
    log.debug("Returning collection with " + deduped.size + " items")
    deduped
  }

  private def listAlreadyContainsResourceWithThisLocation(deduped: Seq[Newsitem], geocode: Geocode): Boolean = {
    deduped.exists { r =>
      r.geocode.forall { rp =>
        areSameOrOverlappingLocations(rp, geocode)
      }
    }
  }

  private def areSameOrOverlappingLocations(here: Geocode, there: Geocode) = {
    def latLongFor(geocode: Geocode): Option[LatLong] = {
      for {
        lat <- geocode.latitude
        lon <- geocode.longitude
      } yield {
        new LatLong(lat, lon)
      }
    }

    for {
      hereLatLong <- latLongFor(here)
      thereLatLong <- latLongFor(there)
    } yield {
      val distanceBetweenHereAndThere = distanceMeasuringService.getDistanceBetween(hereLatLong, thereLatLong)
      distanceBetweenHereAndThere < ONE_HUNDRED_METERS
    }
  }.getOrElse(false)

}
