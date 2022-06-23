package nz.co.searchwellington.views

import org.joda.time.LocalDate
import org.springframework.stereotype.Component

@Component
class DateFormatter extends uk.co.eelpieconsulting.common.dates.DateFormatter("Europe/London") {


  def formatLocalDate(localDate: LocalDate): String = {

    localDate.toString("d MMM yyyy")
  }


}
