package nz.co.searchwellington.utils

import org.apache.commons.lang.StringUtils

trait StringWrangling {

  def lowerCappedSentence(input: String): String = {
    if (isCapitalised(input)) {
      StringUtils.capitalize(StringUtils.lowerCase(input))
    } else {
      input
    }
  }

  def trimToCharacterCount(description: String, maxLength: Int): String = {
    if (description.length > maxLength) {
      val trimmed = description.substring(0, maxLength)
      if (trimmed.contains(".")) return StringUtils.substringBefore(trimmed, ".") + "."
    }
    description
  }

  private def isCapitalised(input: String) = input == StringUtils.upperCase(input)

}
