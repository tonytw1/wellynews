package nz.co.searchwellington.feeds

import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem
import nz.co.searchwellington.model.{Feed, Image, Newsitem, NewsitemImpl}
import nz.co.searchwellington.utils.TextTrimmer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.geo.model.Place

@Component
class FeednewsItemToNewsitemService @Autowired() (textTrimmer: TextTrimmer, placeToGeocodeMapper: PlaceToGeocodeMapper) {

  private val MAXIMUM_BODY_LENGTH: Int = 400

  def makeNewsitemFromFeedItem(feed: Feed, feedNewsitem: FrontendFeedNewsitem): Newsitem = {

    val newsitem = NewsitemImpl(title = Some(feedNewsitem.getName), page = Some(feedNewsitem.getUrl), description = Some(composeDescription(feedNewsitem)),
      date = null, publisher = feed.getPublisher) // TODO date
    newsitem.setImage(if (feedNewsitem.getFrontendImage != null) new Image(feedNewsitem.getFrontendImage.getUrl, null) else null)
    newsitem.setFeed(feed)
    newsitem.setPublisher(feed.getPublisher)

    val place: Place = feedNewsitem.getPlace
    if (place != null) {
      newsitem.setGeocode(placeToGeocodeMapper.mapPlaceToGeocode(place))
    }
    if (feedNewsitem.getFrontendImage != null) {
      newsitem.setImage(new Image(feedNewsitem.getFrontendImage.getUrl, ""))
    }
    return newsitem
  }

  private def composeDescription(feedNewsitem: FrontendFeedNewsitem): String = {
    var description: String = if (feedNewsitem.getDescription != null) feedNewsitem.getDescription else ""
    description = textTrimmer.trimToCharacterCount(description, MAXIMUM_BODY_LENGTH)
    return description
  }

}
