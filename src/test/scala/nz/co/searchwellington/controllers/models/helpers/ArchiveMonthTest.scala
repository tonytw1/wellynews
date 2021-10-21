package nz.co.searchwellington.controllers.models.helpers

import org.joda.time.{DateTime, DateTimeZone, Interval}
import org.junit.Assert.assertEquals
import org.junit.Test

class ArchiveMonthTest extends ArchiveMonth {

  @Test
  def canParseYearMonthUrlStubsIntoIntervals(): Unit = {
    val parsedInterval = parseYearMonth("2021-oct")

    val oct2021 = new DateTime(2021, 10, 1, 0, 0, DateTimeZone.UTC)
    assertEquals(Some(new Interval(oct2021, oct2021.plusMonths(1))), parsedInterval)
  }

  @Test
  def shouldIgnoreDatesWithTrailingCharacters(): Unit = {
    val parsedInterval = parseYearMonth("2021-oct123")
    assertEquals(None, parsedInterval)
  }

}
