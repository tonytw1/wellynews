package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.{Feed, Newsitem, User}
import nz.co.searchwellington.utils.{TextTrimmer, UrlFilters}
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

@Component
class FeeditemToNewsitemService @Autowired()(textTrimmer: TextTrimmer, placeToGeocodeMapper: PlaceToGeocodeMapper) {

  private val MAXIMUM_BODY_LENGTH = 400

  def makeNewsitemFromFeedItem(feedItem: FeedItem, feed: Feed): Newsitem = {
    val newsitem = Newsitem(
      title = Some(feedItem.getTitle),
      page = Some(feedItem.getUrl),
      description = Some(composeDescription(feedItem)),
      date = Some(feedItem.getDate),
      publisher = feed.publisher,
      feed = Some(feed._id),
      geocode = Option(feedItem.getPlace).map(placeToGeocodeMapper.mapPlaceToGeocode)
    )
    // newsitem.setImage(if (feedNewsitem.getFrontendImage != null) new Image(feedNewsitem.getFrontendImage.getUrl, null) else null)

    if (feedItem.getImageUrl != null) {
      // newsitem.setImage(new Image(feedNewsitem.getFrontendImage.getUrl, ""))
    }
    newsitem
  }

  private def composeDescription(feedNewsitem: FeedItem): String = {
    var description = if (feedNewsitem.getBody != null) feedNewsitem.getBody else ""
    textTrimmer.trimToCharacterCount(description, MAXIMUM_BODY_LENGTH)
  }

  private def flattenLoudCapsInTitle(title: String) = { // TODO unused
    UrlFilters.lowerCappedSentence(title)
  }


}
