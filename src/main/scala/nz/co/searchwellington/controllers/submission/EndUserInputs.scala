package nz.co.searchwellington.controllers.submission

import nz.co.searchwellington.urls.UrlCleaner
import nz.co.searchwellington.utils.StringWrangling

trait EndUserInputs extends StringWrangling {

  def urlCleaner: UrlCleaner

  def cleanUrl(url: String): String = {
    urlCleaner.cleanSubmittedItemUrl(url)
  }

  def processTitle(t: String): String = {
    lowerCappedSentence(t)
  }

}
