package nz.co.searchwellington.controllers.models.helpers

import org.joda.time.{DateTime, DateTimeZone, Interval}
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test

class ArchiveMonthTest extends ArchiveMonth {

  private val firstOct2021 = new DateTime(2021, 10, 1, 0, 0, DateTimeZone.UTC)
  val oct2021 = new Interval(firstOct2021, firstOct2021.plusMonths(1))

  @Test
  def canParseYearMonthUrlStubsIntoIntervals(): Unit = {
    val parsedInterval = parseYearMonth("2021-oct")

    assertEquals(Some(oct2021), parsedInterval)
  }

  @Test
  def shouldIgnoreDatesWithTrailingCharacters(): Unit = {
    val parsedInterval = parseYearMonth("2021-oct123")
    assertTrue(parsedInterval.isEmpty)
  }

  @Test
  def canRenderYearMonthIntervalsAsUrlStubs(): Unit = {
    assertEquals("2021-oct", renderYearMonth(oct2021))
  }

}
