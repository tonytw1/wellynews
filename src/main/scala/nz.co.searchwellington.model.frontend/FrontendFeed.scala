package nz.co.searchwellington.model.frontend

import java.util.Date

import nz.co.searchwellington.model.FeedAcceptancePolicy

class FrontendFeed extends FrontendResource { // TODO migrate to immutable constructor

  private var urlWords: String = null
  private var latestItemDate: Date = null
  private var acceptancePolicy: FeedAcceptancePolicy = null

  final override def getUrlWords: String = {
    return urlWords
  }

  final def getLatestItemDate: Date = {
    return latestItemDate
  }

  final def getAcceptancePolicy: FeedAcceptancePolicy = {
    return acceptancePolicy
  }

}
