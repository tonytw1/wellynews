package nz.co.searchwellington.feeds

import java.util.UUID

import nz.co.searchwellington.model.{Feed, Geocode, Newsitem}
import nz.co.searchwellington.utils.{TextTrimmer, UrlFilters}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

@Component
class FeeditemToNewsitemService @Autowired()(textTrimmer: TextTrimmer, placeToGeocodeMapper: PlaceToGeocodeMapper) {

  private val MAXIMUM_BODY_LENGTH: Int = 400

  def makeNewsitemFromFeedItem(feedItem: FeedItem, feed: Option[Feed]): Newsitem = {
    val newsitem = Newsitem(
      id = UUID.randomUUID().toString,
      title = Some(feedItem.getTitle), page = Some(feedItem.getUrl),
      description = Some(composeDescription(feedItem)),
      date = Some(feedItem.getDate), publisher = None,
      feed = feed.map(f => f.id),
      geocode = Option(feedItem.getPlace).map(placeToGeocodeMapper.mapPlaceToGeocode)
    ) // TODO publisher
    // newsitem.setImage(if (feedNewsitem.getFrontendImage != null) new Image(feedNewsitem.getFrontendImage.getUrl, null) else null)

    // newsitem.setPublisher(feed.getPublisher)

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
