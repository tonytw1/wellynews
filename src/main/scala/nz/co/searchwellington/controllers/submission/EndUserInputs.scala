package nz.co.searchwellington.controllers.submission

import nz.co.searchwellington.urls.UrlCleaner

trait EndUserInputs {

  def urlCleaner: UrlCleaner

  def cleanUrl(url: String): String = {
    urlCleaner.cleanSubmittedItemUrl(url)
  }

}
