package nz.co.searchwellington.views

import nz.co.searchwellington.model.{Geocode, Newsitem}
import org.apache.log4j.Logger
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.geo.DistanceMeasuringService

@Component class GoogleMapsDisplayCleaner() {

  private val ONE_HUNDRED_METERS = 0.1
  private val log = Logger.getLogger(classOf[GoogleMapsDisplayCleaner])

  private val distanceMeasuringService = new DistanceMeasuringService

  def dedupe(geocoded: Seq[Newsitem], selected: Option[Newsitem] = None): Seq[Newsitem] = {
    log.debug("Deduping collection with " + geocoded.size + " items")
    var deduped = Seq(selected).flatten

    geocoded.map { resource =>
      resource.geocode.map { p =>
        val isUnique = !listAlreadyContainsResourceWithThisLocation(deduped, p)
        if (isUnique) deduped = deduped :+ resource
      }
    }
    log.debug("Returning collection with " + deduped.size + " items")
    deduped
  }

  private def listAlreadyContainsResourceWithThisLocation(deduped: Seq[Newsitem], geocode: Geocode): Boolean = {
    deduped.exists { r =>
      r.geocode.map { rp =>
        val overlaps = areSameOrOverlappingLocations(rp, geocode)
        overlaps

      }.getOrElse {
        true
      }
    }
  }

  private def areSameOrOverlappingLocations(here: Geocode, there: Geocode) = {
    //val distanceBetweenHereAndThere = distanceMeasuringService.getDistanceBetween(here.getLatLong, there.getLatLong)
    //distanceBetweenHereAndThere < ONE_HUNDRED_METERS
    false // TODO
  }

}
