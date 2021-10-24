package nz.co.searchwellington.controllers.models.helpers

import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone, Interval}

import java.util.Date

trait ArchiveMonth {

  val archiveMonthFormat = DateTimeFormat.forPattern("yyyy-MMM").withZone(DateTimeZone.UTC)

  def parseYearMonth(archiveMonthString: String): Option[Interval] = {
    def intervalForMonth(month: Date): Interval = {
      val start = new DateTime(month, DateTimeZone.UTC)
      new Interval(start, start.plusMonths(1))
    }

    try {
      val month = archiveMonthFormat.parseDateTime(archiveMonthString)
      Some(intervalForMonth(month.toDate))
    }
    catch {
      case _: IllegalArgumentException =>
        None
    }
  }

  def renderYearMonth(interval: Interval): String = {
    archiveMonthFormat.print(interval.getStart).toLowerCase
  }

}
