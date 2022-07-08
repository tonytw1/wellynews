package nz.co.searchwellington.views

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

import java.time.LocalDate
import org.springframework.stereotype.Component

import java.time.format.DateTimeFormatter
import java.util.Date

@Component
class DateFormatter extends uk.co.eelpieconsulting.common.dates.DateFormatter("Europe/London") {

  private val localDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

  def formatLocalDate(localDate: LocalDate): String = {
    localDate.format(localDateFormatter)
  }

  def isoDate(date: Date): String = {
    ISODateTimeFormat.date().print(new DateTime(date))
  }

}
