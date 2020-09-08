package nz.co.searchwellington.controllers.ajax

import nz.co.searchwellington.model.Website
import org.springframework.stereotype.Component

@Component
class TitleTrimmer {

  private val possibleSeparators = Set("-", "|")

  def trimTitle(title: String, publisher: Option[Website]): String = {
    val publisherName = publisher.flatMap(_.title)
    val trimmedTitle = publisherName.flatMap{ pn =>
      val possiblePublisherNameSuffixes = possibleSeparators.map { _ + " " + pn}
      val suffixToRemove = possiblePublisherNameSuffixes.find(s => title.endsWith(s))
      suffixToRemove.map { s =>
        title.substring(0, (title.length - s.length) - 1)
      }
    }

    trimmedTitle.getOrElse(title)
  }

}
