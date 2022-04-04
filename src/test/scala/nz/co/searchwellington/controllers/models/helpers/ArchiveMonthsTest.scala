package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.model.ArchiveLink
import org.joda.time.{DateTime, DateTimeZone, Interval}
import org.junit.Assert.assertEquals
import org.junit.Test

class ArchiveMonthsTest extends ArchiveMonths {

  @Test
  def canFindPreviousAndNextMonthLinks(): Unit = {
    val june = new DateTime(2020, 6, 1, 0, 0, DateTimeZone.UTC)
    val july = june.plusMonths(1)
    val august = july.plusMonths(1)

    val monthOfJune = new Interval(june, june.plusMonths(1))
    val monthOfJuly = new Interval(july, july.plusMonths(1))
    val monthOfAugust = new Interval(august, august.plusMonths(1))

    val archiveLinks = Seq(
        ArchiveLink(monthOfJune, 1L),
        ArchiveLink(monthOfJuly, 2L),
        ArchiveLink(monthOfAugust, 3L),
      )

    val mv = populateNextAndPreviousLinks(monthOfJuly, archiveLinks)

    assertEquals( ArchiveLink(monthOfJune, 1L), mv.get("previous_month"))
    assertEquals( ArchiveLink(monthOfAugust, 3L), mv.get("next_month"))
  }

}
