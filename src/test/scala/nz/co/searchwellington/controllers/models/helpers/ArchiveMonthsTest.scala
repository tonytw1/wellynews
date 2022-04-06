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
    val august = june.plusMonths(2)
    val november = july.plusMonths(4)
    val december = july.plusMonths(5)

    val monthOfJune = new Interval(june, june.plusMonths(1))
    val monthOfJuly = new Interval(july, july.plusMonths(1))
    val monthOfAugust = new Interval(august, august.plusMonths(1))
    val monthOfNovember = new Interval(november, november.plusMonths(1))
    val monthOfDecember = new Interval(december, december.plusMonths(1))

    val archiveLinks = Seq(
      ArchiveLink(monthOfJune, 1L),
      ArchiveLink(monthOfJuly, 2L),
      ArchiveLink(monthOfAugust, 1L),
      ArchiveLink(monthOfNovember, 1L),
      ArchiveLink(monthOfDecember, 3L),
    )

    val mv = populateNextAndPreviousLinks(monthOfAugust, archiveLinks)

    assertEquals(ArchiveLink(monthOfJuly, 2L), mv.get("previous_month"))
    assertEquals(ArchiveLink(monthOfNovember, 1L), mv.get("next_month"))
  }

}
