package nz.co.searchwellington.model.frontend

import java.util.Date

import nz.co.searchwellington.model.{FeedAcceptancePolicy, Geocode, Tag}

case class FrontendFeed(id: String,
                        urlWords: String = null,
                        `type`: String = null,
                        name: String = null,
                        url: String = null,
                        httpStatus: Int = 0,
                        date: Date = null,
                        description: String = null,
                        liveTime: Date = null,
                        tags: Seq[Tag] = Seq.empty,
                        handTags: Seq[Tag] = Seq.empty,
                        owner: String = null,
                        place: Option[Geocode] = None,
                        held: Boolean = false,
                        latestItemDate: Date = null,
                        acceptancePolicy: FeedAcceptancePolicy = null,
                        lastRead: Option[Date] = None,
                        publisher: Option[FrontendWebsite] = None,
                        lastScanned: Option[Date] = None,
                        lastChanged: Option[Date] = None
                       ) extends FrontendResource {

  def getLatestItemDate: Date = {
    latestItemDate
  }

  def getAcceptancePolicy: FeedAcceptancePolicy = {
    acceptancePolicy
  }

  def getLastRead: Date = {
    lastRead.orNull
  }

  def getPublisher: FrontendWebsite = publisher.orNull

}
