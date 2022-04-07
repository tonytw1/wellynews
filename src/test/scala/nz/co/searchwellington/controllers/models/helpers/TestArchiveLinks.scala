package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.model.ArchiveLink
import org.joda.time.{DateTime, DateTimeZone, Interval}

trait TestArchiveLinks {

  def someArchiveMonths: Seq[ArchiveLink] = {
    val january = new DateTime(2021, 1, 1, 0, 0, 0, 0)
    val start = new DateTime(january, DateTimeZone.UTC)
    val a = ArchiveLink(count = 12L, interval = new Interval(start, start.plusMonths(1)))
    val b = ArchiveLink(count = 24L, interval = new Interval(start.plusMonths(1), start.plusMonths(2)))
    val c = ArchiveLink(count = 24L, interval = new Interval(start.plusMonths(3), start.plusMonths(3)))
    val archiveLinks = Seq(a, b, c)
    archiveLinks
  }

}
