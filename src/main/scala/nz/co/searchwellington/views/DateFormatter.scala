package nz.co.searchwellington.views

import java.time.LocalDate
import org.springframework.stereotype.Component

import java.time.format.DateTimeFormatter

@Component
class DateFormatter extends uk.co.eelpieconsulting.common.dates.DateFormatter("Europe/London") {

  private val localDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

  def formatLocalDate(localDate: LocalDate): String = {
    localDate.format(localDateFormatter)
  }

}
