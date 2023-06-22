package nz.co.searchwellington.views

import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.geo.Geocode
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.geo.DistanceMeasuringService
import uk.co.eelpieconsulting.common.geo.model.LatLong

import java.util.Date
import scala.jdk.CollectionConverters._

@Component
class MapPinDeduplicator() {

  private val ONE_HUNDRED_METERS = 0.1

  private val distanceMeasuringService = new DistanceMeasuringService

  def dedupe(geocoded: java.util.List[FrontendResource]): java.util.List[FrontendResource] = {
    // Allow most recent items to trump older items with the same location.
    val byDateDescending = geocoded.asScala.sortBy(r => Option(r.date).getOrElse(new Date(0L))).reverse

    byDateDescending.foldLeft(Seq.empty[FrontendResource]) { (deduplicated, resource) =>
      val isUniquePoint = resource.geocode.exists { geocode =>
        !listAlreadyContainsResourceWithThisLocation(deduplicated, geocode)
      }
      if (isUniquePoint) {
        deduplicated :+ resource
      } else {
        deduplicated
      }
    }.asJava
  }

  private def listAlreadyContainsResourceWithThisLocation(deduped: Seq[FrontendResource], geocode: Geocode): Boolean = {
    deduped.exists { r =>
      r.geocode.forall { rp =>
        areSameOrOverlappingLocations(rp, geocode)
      }
    }
  }

  private def areSameOrOverlappingLocations(here: Geocode, there: Geocode): Boolean = {
    def latLongFor(geocode: Geocode): Option[LatLong] = {
      geocode.latLong.map { ll =>
        new uk.co.eelpieconsulting.common.geo.model.LatLong(ll.latitude, ll.longitude)
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
