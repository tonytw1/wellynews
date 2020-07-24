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

  private def isCapitalised(input: String): Boolean = input == StringUtils.upperCase(input)

}
