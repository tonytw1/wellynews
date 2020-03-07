package nz.co.searchwellington.views

import nz.co.searchwellington.model.{Geocode, Newsitem}
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.Test

class GoogleMapsDisplayCleanerTest {
  private val here = Geocode(Some("here"), Some(1.1), Some(1.1))
  private val there = Geocode(Some("there"), Some(2.2), Some(2.2))
  private val alsoHere = Geocode(Some("here"), Some(1.1), Some(1.1))
  private val firstNewsitem = Newsitem(title = Some("First"), geocode = Some(here))
  private val secondNewsitem = Newsitem(title = Some("Second"), geocode = Some(there))
  private val thirdNewsitem = Newsitem(title = Some("Third"), geocode = Some(alsoHere))
  private val geocoded = Seq(firstNewsitem, secondNewsitem, thirdNewsitem)

  private val cleaner = new GoogleMapsDisplayCleaner()

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
