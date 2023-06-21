package nz.co.searchwellington.views

import nz.co.searchwellington.model.frontend.{FrontendNewsitem, FrontendResource}
import nz.co.searchwellington.model.geo.{Geocode, LatLong}
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.{assertEquals, assertFalse, assertTrue}
import org.junit.jupiter.api.Test

import java.util
import java.util.UUID

import scala.jdk.CollectionConverters._

class MapPinDeduplicatorTest {
  private val here = Geocode(Some("here"), Some(LatLong(-41.2924, 174.7787)))
  private val there = Geocode(Some("there"), Some(LatLong(2.2, 2.2)))
  private val alsoHere = Geocode(Some("here"), Some(LatLong(-41.2924, 174.7788)))

  private val firstNewsitem = FrontendNewsitem(name = "First", geocode = Some(here), date = DateTime.now().minusDays(5).toDate, id = UUID.randomUUID().toString, url = "")
  private val secondNewsitem = FrontendNewsitem(name = "Second", geocode = Some(there), date = DateTime.now().minusDays(5).toDate, id = UUID.randomUUID().toString, url = "")
  private val thirdNewsitem = FrontendNewsitem(name = "Third", geocode = Some(alsoHere), date = DateTime.now().toDate, id = UUID.randomUUID().toString, url = "")

  private val geocoded: Seq[FrontendResource] = Seq(firstNewsitem, secondNewsitem, thirdNewsitem)

  private val mapPinDeduplicator = new MapPinDeduplicator()

  @Test
  def shouldProtectItemsWithUniqueLocations(): Unit = {
    val deduped: util.List[FrontendResource] = mapPinDeduplicator.dedupe(geocoded.asJava)

    assertEquals(2, deduped.size)

    assertTrue(deduped.contains(secondNewsitem), "Expected item to survive because it has a unique and distant location")
  }

  @Test
  def shouldPreferMostRecentlyPublishedItems(): Unit = {
    val deduped = mapPinDeduplicator.dedupe(geocoded.asJava)

    assertEquals(2, deduped.size)

    assertTrue(deduped.contains(thirdNewsitem), "Expected item to survive because it has the most recent date")
    assertFalse(deduped.contains(firstNewsitem), "Expected item to survive because it has the most recent date")
  }

  @Test
  def shouldGracefullyHandleNewsitemsWithNoDate(): Unit = {
    val withNoLatLong = firstNewsitem.copy(date = null)
    val deduped = mapPinDeduplicator.dedupe((geocoded :+ withNoLatLong).asJava)

    assertFalse(deduped.contains(withNoLatLong), "Expected item to survive because it has the most recent date")
  }

}
