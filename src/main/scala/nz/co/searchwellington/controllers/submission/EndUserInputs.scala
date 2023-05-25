package nz.co.searchwellington.controllers.submission

import nz.co.searchwellington.urls.{UrlCleaner, UrlFilters}
import nz.co.searchwellington.utils.StringWrangling

import java.net.URL
import scala.util.Try

// Functions to apply to unclean end user inputs
trait EndUserInputs extends StringWrangling {

  def urlCleaner: UrlCleaner

  // Given a user supplied url string provide any remedial work which might produce a more parsable URL.
  def cleanUrl(urlString: String): Either[Throwable, URL] = {
    // Trim and add prefix is missing from user submitted input
    val cleanedString = UrlFilters.addHttpPrefixIfMissing(urlString.trim)
    Try {
      val url = new URL(cleanedString)
      urlCleaner.cleanSubmittedItemUrl(url)
    }.toEither
  }

  def trimToOption(i: String): Option[String] = {
    // Given a non null string check that it's not just blank space
    if (i.trim.nonEmpty) {
      Some(i.trim)
    } else {
      None
    }
  }

  def processTitle(t: String): String = {
    lowerCappedSentence(t).trim
  }

}
