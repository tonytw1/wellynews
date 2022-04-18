package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.model.IntervalLink
import nz.co.searchwellington.model.frontend.FrontendResource
import org.joda.time.{DateTime, Interval, YearMonth}
import org.springframework.ui.ModelMap

trait ArchiveMonths {

  def populateNextAndPreviousLinks(currentMonth: Interval, archiveLinks: Seq[IntervalLink]): ModelMap = {
    // Given the ordered list of all available archive link months and the current month,
    // populate the previous and next links (if available)

    val sorted = archiveLinks.sortBy(_.interval.getStart)
    val next = sorted.find(_.interval.isAfter(currentMonth))
    val previous = sorted.filter(_.interval.isBefore(currentMonth)).lastOption

    val mv = new ModelMap()
    previous.foreach(p =>
      mv.addAttribute("previous_month", p)
    )
    next.foreach(n =>
      mv.addAttribute("next_month", n)
    )
    mv
  }

  def monthOfLastItem(newsitems: Seq[FrontendResource]): Option[Interval] = newsitems.lastOption.map { i =>
    new YearMonth(new DateTime(i.date)).toInterval()
  }

}
