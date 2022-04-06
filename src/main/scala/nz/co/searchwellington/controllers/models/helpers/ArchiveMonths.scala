package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.model.{ArchiveLink, TagArchiveLink}
import org.joda.time.Interval
import org.springframework.ui.ModelMap

trait ArchiveMonths {

  def populateNextAndPreviousLinks(currentMonth: Interval, archiveLinks: Seq[ArchiveLink]): ModelMap = {
    // Given the ordered list of all available archive link months and the current month,
    // populate the previous and next links (if available)
    val previous = archiveLinks.filter(_.interval.isBefore(currentMonth)).lastOption
    val next = archiveLinks.find(_.interval.isAfter(currentMonth))
    val mv = new ModelMap()
    previous.foreach(p =>
      mv.addAttribute("previous_month", p)
    )
    next.foreach(n =>
      mv.addAttribute("next_month", n)
    )
    mv
  }

  // TODO obvious inheritance here
  def populateNextAndPreviousTagLinks(currentMonth: Interval, archiveLinks: Seq[TagArchiveLink]): ModelMap = {
    // Given the ordered list of all available archive link months and the current month,
    // populate the previous and next links (if available)
    val previous = archiveLinks.filter(_.interval.isBefore(currentMonth)).lastOption
    val next = archiveLinks.find(_.interval.isAfter(currentMonth))
    val mv = new ModelMap()
    previous.foreach(p =>
      mv.addAttribute("previous_month", p)
    )
    next.foreach(n =>
      mv.addAttribute("next_month", n)
    )
    mv
  }

}
