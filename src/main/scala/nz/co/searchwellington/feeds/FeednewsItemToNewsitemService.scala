package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem
import nz.co.searchwellington.model.{Feed, Newsitem}
import nz.co.searchwellington.utils.TextTrimmer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class FeednewsItemToNewsitemService @Autowired() (textTrimmer: TextTrimmer, placeToGeocodeMapper: PlaceToGeocodeMapper) {

  private val MAXIMUM_BODY_LENGTH: Int = 400

  def makeNewsitemFromFeedItem(feed: Feed, feedNewsitem: FrontendFeedNewsitem): Newsitem = {

    val newsitem = Newsitem(title = Some(feedNewsitem.getName), page = Some(feedNewsitem.getUrl), description = Some(composeDescription(feedNewsitem)),
      date2 = Some(feedNewsitem.getDate), publisher = None) // TODO publisher
    // newsitem.setImage(if (feedNewsitem.getFrontendImage != null) new Image(feedNewsitem.getFrontendImage.getUrl, null) else null)
    // newsitem.setFeed(feed)
    // newsitem.setPublisher(feed.getPublisher)

    val place = feedNewsitem.getPlace
    if (place != null) {
      // newsitem.setGeocode(placeToGeocodeMapper.mapPlaceToGeocode(place))
    }
    if (feedNewsitem.getFrontendImage != null) {
      // newsitem.setImage(new Image(feedNewsitem.getFrontendImage.getUrl, ""))
    }
    newsitem
  }

  private def composeDescription(feedNewsitem: FrontendFeedNewsitem): String = {
    var description = if (feedNewsitem.getDescription != null) feedNewsitem.getDescription else ""
    textTrimmer.trimToCharacterCount(description, MAXIMUM_BODY_LENGTH)
  }

}
