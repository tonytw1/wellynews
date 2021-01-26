package nz.co.searchwellington.views

import nz.co.searchwellington.model.{Geocode, Newsitem}
import org.apache.log4j.Logger
import org.locationtech.spatial4j.distance.{DistanceCalculator, GeodesicSphereDistCalc}
import org.locationtech.spatial4j.shape.Point
import org.locationtech.spatial4j.shape.impl.PointImpl
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.geo.model.LatLong

@Component class MapPinDeduplicator() {

  private val ONE_HUNDRED_METERS = 0.1
  private val log = Logger.getLogger(classOf[MapPinDeduplicator])

  def dedupe(geocoded: Seq[Newsitem], selected: Option[Newsitem] = None): Seq[Newsitem] = {
    log.debug("Deduping collection with " + geocoded.size + " items")
    var deduped = Seq(selected).flatten

    geocoded.map { resource =>
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

      import org.locationtech.spatial4j.context.SpatialContext
      val ctx = SpatialContext.GEO

      val h = ctx.makePoint(hereLatLong.getLatitude, hereLatLong.getLongitude)
      val t = ctx.makePoint(thereLatLong.getLatitude, thereLatLong.getLongitude)

      val distanceBetweenHereAndThere = ctx.calcDistance(h, t)
      distanceBetweenHereAndThere < ONE_HUNDRED_METERS
    }
  }.getOrElse(false)

}
