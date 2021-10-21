package nz.co.searchwellington.controllers.models.helpers

import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone, Interval}

import java.text.SimpleDateFormat
import java.util.Date

trait ArchiveMonth {

  @Deprecated // Timezone issues here
  val archiveMonthFormat = new SimpleDateFormat("yyyy-MMM")

  val amf = DateTimeFormat.forPattern("yyyy-MMM").withZone(DateTimeZone.UTC)

  def parseYearMonth(archiveMonthString: String): Option[Interval] = {
    def intervalForMonth(month: Date): Interval = {
      val start = new DateTime(month, DateTimeZone.UTC)
      new Interval(start, start.plusMonths(1))
    }

    try {
      val month = amf.parseDateTime(archiveMonthString)  // TODO This parse over matches; ie. 2021-sep2?
      Some(intervalForMonth(month.toDate))
    }
    catch {
      case _: IllegalArgumentException =>
        None
    }
  }

}
