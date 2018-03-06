package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.{Feed, Newsitem}
import nz.co.searchwellington.utils.{TextTrimmer, UrlFilters}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.whakaoro.client.model.FeedItem

@Component
class FeeditemToNewsitemService @Autowired()(textTrimmer: TextTrimmer, placeToGeocodeMapper: PlaceToGeocodeMapper) {

  private val MAXIMUM_BODY_LENGTH: Int = 400

  def makeNewsitemFromFeedItem(feed: Feed, feedNewsitem: FeedItem): Newsitem = {  // TODO user as well

    val newsitem = Newsitem(title = Some(feedNewsitem.getTitle), page = Some(feedNewsitem.getUrl), description = Some(composeDescription(feedNewsitem)),
      date2 = Some(feedNewsitem.getDate), publisher = None) // TODO publisher
    // newsitem.setImage(if (feedNewsitem.getFrontendImage != null) new Image(feedNewsitem.getFrontendImage.getUrl, null) else null)
    // newsitem.setFeed(feed)
    // newsitem.setPublisher(feed.getPublisher)

    val place = feedNewsitem.getPlace
    if (place != null) {
      // newsitem.setGeocode(placeToGeocodeMapper.mapPlaceToGeocode(place))
    }
    if (feedNewsitem.getImageUrl != null) {
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
