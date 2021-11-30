package nz.co.searchwellington.feeds

import nz.co.searchwellington.controllers.submission.EndUserInputs
import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.model.{Feed, Newsitem}
import nz.co.searchwellington.urls.UrlCleaner
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class FeeditemToNewsitemService @Autowired()(placeToGeocodeMapper: PlaceToGeocodeMapper, val urlCleaner: UrlCleaner) extends EndUserInputs {

  private val MAXIMUM_BODY_LENGTH = 400

  def makeNewsitemFromFeedItem(feedItem: FeedItem, feed: Feed): Newsitem = {
    val newsitem = Newsitem(
      title = feedItem.title.map(processTitle),
      page = cleanUrl(feedItem.url),
      description = Some(composeDescription(feedItem)),
      date = feedItem.date.map(_.toDate),
      feed = Some(feed._id),
      publisher = feed.publisher,
      geocode = feedItem.place.map(placeToGeocodeMapper.mapPlaceToGeocode)
    )
    // newsitem.setImage(if (feedNewsitem.getFrontendImage != null) new Image(feedNewsitem.getFrontendImage.getUrl, null) else null)

    if (feedItem.imageUrl != null) {  // TODO option
      // newsitem.setImage(new Image(feedNewsitem.getFrontendImage.getUrl, ""))
    }

    newsitem
  }

  private def composeDescription(feedNewsitem: FeedItem): String = {
    feedNewsitem.body.map { description =>
      trimToCharacterCount(description, MAXIMUM_BODY_LENGTH)
    }.getOrElse("")
  }

}
