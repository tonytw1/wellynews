package nz.co.searchwellington.model.frontend

import java.util.Date

class FrontendFeed extends FrontendResource {

  private var publisherName: String = null
  private var urlWords: String = null
  private var latestItemDate: Date = null

  final def getPublisherName: String = {
    return publisherName
  }

  final def setPublisherName(publisherName: String) {
    this.publisherName = publisherName
  }

  final override def getUrlWords: String = {
    return urlWords
  }

  final override def setUrlWords(urlWords: String) {
    this.urlWords = urlWords
  }

  final def getLatestItemDate: Date = {
    return latestItemDate
  }

  final def setLatestItemDate(latestItemDate: Date) {
    this.latestItemDate = latestItemDate
  }

}