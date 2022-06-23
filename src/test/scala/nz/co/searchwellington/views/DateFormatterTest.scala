package nz.co.searchwellington.views

import org.joda.time.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DateFormatterTest {
  @Test
  def shouldFormatLocalDates() = {
    assertEquals("7 Mar 2022", new DateFormatter().formatLocalDate(new LocalDate(2022, 3, 7)))
  }
}
