package nz.co.searchwellington.views

import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.model.geo.Geocode
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.geo.DistanceMeasuringService
import uk.co.eelpieconsulting.common.geo.model.LatLong

import scala.jdk.CollectionConverters._

@Component
class MapPinDeduplicator() {

  private val ONE_HUNDRED_METERS = 0.1

  private val distanceMeasuringService = new DistanceMeasuringService

  def dedupe(geocoded: java.util.List[FrontendResource]): java.util.List[FrontendResource] = {
    var deduped: Seq[FrontendResource] = Seq.empty

    val byDateDescending = geocoded.asScala.sortBy(r => r.date).reverse

    byDateDescending.foreach { resource =>
      resource.geocode.foreach { p =>
        val isUnique = !listAlreadyContainsResourceWithThisLocation(deduped, p)
        if (isUnique) {
          deduped = deduped :+ resource
        }
      }
    }
    deduped.asJava
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
