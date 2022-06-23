package nz.co.searchwellington.views

import java.time.LocalDate
import org.springframework.stereotype.Component

import java.time.format.DateTimeFormatter

@Component
class DateFormatter extends uk.co.eelpieconsulting.common.dates.DateFormatter("Europe/London") {

  def formatLocalDate(localDate: LocalDate): String = {
    localDate.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
  }

}
