package nz.co.searchwellington.feeds

import nz.co.searchwellington.controllers.submission.EndUserInputs
import nz.co.searchwellington.feeds.whakaoko.model.FeedItem
import nz.co.searchwellington.model.{Feed, Newsitem}
import nz.co.searchwellington.urls.UrlCleaner
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class FeeditemToNewsitemService @Autowired()(placeToGeocodeMapper: PlaceToGeocodeMapper, val urlCleaner: UrlCleaner) extends EndUserInputs {

  private val MAXIMUM_BODY_LENGTH = 400

  def makeNewsitemFromFeedItem(feedItem: FeedItem, feed: Feed): Option[Newsitem] = {
    cleanUrl(feedItem.url).toOption.map { url =>
      // newsitem.setImage(if (feedNewsitem.getFrontendImage != null) new Image(feedNewsitem.getFrontendImage.getUrl, null) else null)
      //if (feedItem.imageUrl != null) {
      // newsitem.setImage(new Image(feedNewsitem.getFrontendImage.getUrl, ""))
      //}
      Newsitem(
        title = feedItem.title.map(processTitle).getOrElse(feedItem.url),
        page = url.toExternalForm,
        description = Some(composeDescription(feedItem)),
        date = feedItem.date.map(_.toDate).getOrElse(DateTime.now.toDate),  // TODO incorrect for feed items with no date!
        feed = Some(feed._id),
        publisher = feed.publisher,
        geocode = feedItem.place.map(placeToGeocodeMapper.mapPlaceToGeocode)
      )
    }
  }

  private def composeDescription(feedNewsitem: FeedItem): String = {
    feedNewsitem.body.map { description =>
      trimToCharacterCount(description, MAXIMUM_BODY_LENGTH)
    }.getOrElse("")
  }

}
