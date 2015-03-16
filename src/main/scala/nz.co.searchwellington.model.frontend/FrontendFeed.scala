package nz.co.searchwellington.model.frontend

import java.util.Date

import nz.co.searchwellington.model.FeedAcceptancePolicy

class FrontendFeed extends FrontendResource { // TODO migrate to immutable constructor

  private var publisherName: String = null
  private var urlWords: String = null
  private var latestItemDate: Date = null
  private var acceptancePolicy: FeedAcceptancePolicy = null;

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

  final def setAcceptancePolicy(acceptancePolicy: FeedAcceptancePolicy): Unit = {
    this.acceptancePolicy = acceptancePolicy
  }

  final def getAcceptancePolicy: FeedAcceptancePolicy = {
    return acceptancePolicy
  }

}