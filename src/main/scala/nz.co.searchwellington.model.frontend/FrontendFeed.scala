package nz.co.searchwellington.model.frontend

import java.util.{Date, List}

import nz.co.searchwellington.model.FeedAcceptancePolicy
import uk.co.eelpieconsulting.common.geo.model.Place

case class FrontendFeed(id: String,
                        urlWords: String = null,
                        `type`: String = null,
                        name: String = null,
                        url: String = null,
                        httpStatus: Int = 0,
                        date: Date = null,
                        description: String = null,
                        liveTime: Date = null,
                        tags: List[FrontendTag] = null,
                        handTags: List[FrontendTag] = null,
                        owner: String = null,
                        place: Place = null,
                        held: Boolean = false,
                        latestItemDate: Date = null,
                        acceptancePolicy: FeedAcceptancePolicy = null,
                        lastRead: Option[Date] = None
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

}
