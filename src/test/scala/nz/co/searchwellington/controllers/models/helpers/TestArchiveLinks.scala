package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.model.ArchiveLink
import org.joda.time.{DateTime, DateTimeZone, Interval}

trait TestArchiveLinks {

  def someArchiveMonths: Seq[ArchiveLink] = {
    val january = new DateTime(2021, 1, 1, 0, 0, 0, 0)
    val start = new DateTime(january, DateTimeZone.UTC)
    Range.inclusive(0, 2).map { i =>
      ArchiveLink(count = i + 1 * 12L, interval = new Interval(start.plusMonths(i), start.plusMonths(i + 1)))
    }
  }

}
