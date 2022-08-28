package nz.co.searchwellington.views

import nz.co.searchwellington.model.Newsitem
import nz.co.searchwellington.model.geo.{Geocode, LatLong}
import org.junit.jupiter.api.Assertions.{assertEquals, assertFalse, assertTrue}
import org.junit.jupiter.api.Test

class MapPinDeduplicatorTest {
  private val here = Geocode(Some("here"), Some(LatLong(1.1, 1.1)))
  private val there = Geocode(Some("there"), Some(LatLong(2.2, 2.2)))
  private val alsoHere = Geocode(Some("here"), Some(LatLong(1.1, 1.1)))
  private val firstNewsitem = Newsitem(title = "First", geocode = Some(here))
  private val secondNewsitem = Newsitem(title = "Second", geocode = Some(there))
  private val thirdNewsitem = Newsitem(title = "Third", geocode = Some(alsoHere))
  private val geocoded = Seq(firstNewsitem, secondNewsitem, thirdNewsitem)

  private val cleaner = new MapPinDeduplicator()

  @Test
  def shouldDedupeListByGeocodeSoThatLowerItemsDoNotOverlayEarlierOnes(): Unit = {
    val deduped = cleaner.dedupe(geocoded)

    assertEquals(2, deduped.size)
    assertFalse(deduped.contains(thirdNewsitem))
  }

  @Test
  def shouldPutSelectedItemInFirst(): Unit = {
    val selected = Newsitem(geocode = Some(there))

    val deduped = cleaner.dedupe(geocoded, Some(selected))

    assertTrue(deduped.contains(selected))
    assertFalse(deduped.contains(secondNewsitem))
  }

}
