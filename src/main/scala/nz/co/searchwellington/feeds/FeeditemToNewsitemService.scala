package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.{Feed, Newsitem}
import nz.co.searchwellington.utils.{TextTrimmer, UrlFilters}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

@Component
class FeeditemToNewsitemService @Autowired()(textTrimmer: TextTrimmer, placeToGeocodeMapper: PlaceToGeocodeMapper) {

  private val MAXIMUM_BODY_LENGTH: Int = 400

  def makeNewsitemFromFeedItem(feedItem: FeedItem, feed: Option[Feed]): Newsitem = {  // TODO user as well

    val newsitem = Newsitem(title = Some(feedItem.getTitle), page = Some(feedItem.getUrl),
      description = Some(composeDescription(feedItem)),
      date2 = Some(feedItem.getDate), publisher = None,
      feed = feed.map(f => f.id)
    ) // TODO publisher
    // newsitem.setImage(if (feedNewsitem.getFrontendImage != null) new Image(feedNewsitem.getFrontendImage.getUrl, null) else null)

    // newsitem.setPublisher(feed.getPublisher)

    val place = feedItem.getPlace
    if (place != null) {
      // newsitem.setGeocode(placeToGeocodeMapper.mapPlaceToGeocode(place))
    }
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
