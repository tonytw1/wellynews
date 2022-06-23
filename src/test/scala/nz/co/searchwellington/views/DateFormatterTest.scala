package nz.co.searchwellington.views

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

import java.time.LocalDate

class DateFormatterTest {

  @Test
  def shouldFormatLocalDates() = {
    assertEquals("7 Mar 2022", new DateFormatter().formatLocalDate(LocalDate.of(2022, 3, 7)))
  }

}
