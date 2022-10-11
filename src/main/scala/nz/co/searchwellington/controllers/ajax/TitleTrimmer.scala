package nz.co.searchwellington.controllers.ajax

import org.springframework.stereotype.Component

@Component
class TitleTrimmer {

  private val possibleSeparators = Set("-", "|", "—", "–")

  def trimTitleSuffix(title: String, suffix: String): String = {
      val possiblePublisherNameSuffixes = possibleSeparators.map { _ + " " + suffix}

      val suffixToRemove = possiblePublisherNameSuffixes.find(s => title.endsWith(s))
      suffixToRemove.map { s =>
        title.substring(0, (title.length - s.length) - 1)
      }.getOrElse(title)
    }

}
