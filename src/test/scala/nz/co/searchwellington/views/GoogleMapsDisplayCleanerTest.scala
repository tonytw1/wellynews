package nz.co.searchwellington.views

import nz.co.searchwellington.model.{Geocode, Newsitem}
import org.junit.Assert.{assertFalse, assertTrue, assertEquals}
import org.junit.{Before, Test}

class GoogleMapsDisplayCleanerTest {
  private val here = new Geocode(Some("here"), Some(1.1), Some(1.1))
  private val there = new Geocode(Some("there"),  Some(2.2), Some(2.2))
  private val alsoHere = new Geocode(Some("here"),  Some(1.1), Some(1.1))
  private var geocoded: Seq[Newsitem] = null
  private var firstNewsitem: Newsitem = null
  private var secondNewsitem: Newsitem= null
  private var thirdNewsitem: Newsitem = null
  private var cleaner = new GoogleMapsDisplayCleaner()

  @Before
  @throws[Exception]
  def setup(): Unit = {
    firstNewsitem = Newsitem(title = Some("First"), geocode = Some(here))
    secondNewsitem = Newsitem(title = Some("Second"),geocode = Some(there))
    thirdNewsitem = Newsitem(title = Some("Third"), geocode = Some(alsoHere))
    geocoded = Seq(firstNewsitem, secondNewsitem, thirdNewsitem)
    cleaner = new GoogleMapsDisplayCleaner
  }

  @Test
  @throws[Exception]
  def shouldDedupeListByGeocodeSoThatLowerItemsDoNotOverlayEarlierOnes(): Unit = {
    val deduped = cleaner.dedupe(geocoded)

    assertEquals(2, deduped.size)
    assertFalse(deduped.contains(thirdNewsitem))
  }

  @Test
  @throws[Exception]
  def shouldPutSelectedItemInFirst(): Unit = {
    val selected = Newsitem(geocode = Some(there))

    val deduped = cleaner.dedupe(geocoded, Some(selected))

    assertTrue(deduped.contains(selected))
    assertFalse(deduped.contains(secondNewsitem))
  }

}
