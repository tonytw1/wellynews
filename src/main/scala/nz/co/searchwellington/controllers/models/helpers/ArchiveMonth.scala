package nz.co.searchwellington.controllers.models.helpers

import java.text.{ParseException, SimpleDateFormat}
import java.util.Date

import org.joda.time.{DateTime, Interval}

trait ArchiveMonth {

  val archiveMonthFormat = new SimpleDateFormat("yyyy-MMM")

  def parseYearMonth(archiveMonthString: String): Option[Interval] = {
    def intervalForMonth(month: Date): Interval = {
      new Interval(new DateTime(month), new DateTime(month).plusMonths(1))
    }

    try {
      val month = archiveMonthFormat.parse(archiveMonthString)  // TODO This parse over matches; ie. 2021-sep2
      Some(intervalForMonth(month))
    }
    catch {
      case _: ParseException =>
        None
    }
  }

}
