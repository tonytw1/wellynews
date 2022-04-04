package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.model.ArchiveLink
import org.joda.time.Interval
import org.springframework.ui.ModelMap

trait ArchiveMonths {

  def populateNextAndPreviousLinks(currentMonth: Interval, archiveLinks: Seq[ArchiveLink]): ModelMap = {
    // Given the ordered list of all available archive link months and the current month,
    // populate the previous and next links (if available)
    val previousMonth = currentMonth.withStart(currentMonth.getStart.minusMonths(1)).withEnd(currentMonth.getStart)
    val nextMonth = currentMonth.withStart(currentMonth.getStart.plusMonths(1)).withEnd(currentMonth.getStart.plusMonths(2))

    // TODO probably does not work if there are gaps in the sequence.
    val mv = new ModelMap()
    archiveLinks.find { link =>
      link.interval == previousMonth
    }.map { l =>
      mv.addAttribute("previous_month", l)
    }
    archiveLinks.find { link =>
      link.interval == nextMonth
    }.map { l =>
      mv.addAttribute("next_month", l)
    }
    mv
  }

}
