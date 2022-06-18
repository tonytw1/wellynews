package nz.co.searchwellington.controllers.submission

import nz.co.searchwellington.urls.UrlCleaner
import nz.co.searchwellington.utils.{StringWrangling, UrlFilters}

import java.net.URL
import scala.util.Try

// Functions to apply to unclean end user inputs
trait EndUserInputs extends StringWrangling {

  def urlCleaner: UrlCleaner

  // Given a user supplied url string provide any remedial work which might produce a more parsable URL.
  def cleanUrl(urlString: String): String = {
    // Trim and add prefix is missing from user submitted input
    var cleanedString = UrlFilters.trimWhiteSpace(urlString)
    cleanedString = UrlFilters.addHttpPrefixIfMissing(cleanedString)
    Try {
      new URL(cleanedString)
    }.toOption.map { url =>
      urlCleaner.cleanSubmittedItemUrl(url).toExternalForm
    }.getOrElse{
      urlString // TODO error handling
    }
  }

  def processTitle(t: String): String = {
    lowerCappedSentence(t).trim
  }

}
