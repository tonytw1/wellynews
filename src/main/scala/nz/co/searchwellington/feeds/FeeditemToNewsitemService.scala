package nz.co.searchwellington.feeds

import nz.co.searchwellington.feeds.reading.whakaoko.model.FeedItem
import nz.co.searchwellington.model.{Feed, Newsitem}
import nz.co.searchwellington.utils.{TextTrimmer, UrlFilters}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class FeeditemToNewsitemService @Autowired()(textTrimmer: TextTrimmer, placeToGeocodeMapper: PlaceToGeocodeMapper) {

  private val MAXIMUM_BODY_LENGTH = 400

  def makeNewsitemFromFeedItem(feedItem: FeedItem, feed: Feed): Newsitem = {
    val newsitem = Newsitem(
      title = feedItem.title,
      page = Some(feedItem.url),
      description = Some(composeDescription(feedItem)),
      date = feedItem.date.map(_.toDate),
      feed = Some(feed._id),
      publisher = feed.publisher,
      // TODO geocode = Option(feedItem.getPlace).map(placeToGeocodeMapper.mapPlaceToGeocode)
    )
    // newsitem.setImage(if (feedNewsitem.getFrontendImage != null) new Image(feedNewsitem.getFrontendImage.getUrl, null) else null)

    if (feedItem.imageUrl != null) {  // TODO option
      // newsitem.setImage(new Image(feedNewsitem.getFrontendImage.getUrl, ""))
    }
    newsitem
  }

  private def composeDescription(feedNewsitem: FeedItem): String = {
    feedNewsitem.body.map { description =>
      textTrimmer.trimToCharacterCount(description, MAXIMUM_BODY_LENGTH)
    }.getOrElse("")
  }

  private def flattenLoudCapsInTitle(title: String) = { // TODO unused
    UrlFilters.lowerCappedSentence(title)
  }


}
