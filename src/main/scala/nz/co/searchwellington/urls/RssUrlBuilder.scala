package nz.co.searchwellington.urls

import com.google.common.base.Strings
import nz.co.searchwellington.model.frontend.FrontendWebsite
import nz.co.searchwellington.model.{SiteInformation, Tag, Website}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.geo.model.{LatLong, OsmId, Place}

@Component class RssUrlBuilder @Autowired()(siteInformation: SiteInformation) {

  def getBaseRssUrl: String = {
    siteInformation.getUrl + "/rss"
  }

  def getBaseRssTitle: String = {
    siteInformation.getAreaname + " Newslog"
  }

  def getRssUrlForPublisher(publisher: Website): String = {
    siteInformation.getUrl + "/" + publisher.url_words.getOrElse("") + "/rss"
  }
  def getRssUrlForPublisher(publisher: FrontendWebsite): String = {
    siteInformation.getUrl + "/" + publisher.urlWords + "/rss"
  }

  def getRssUrlForTag(tag: Tag): String = {
    siteInformation.getUrl + "/" + tag.getName + "/rss"
  }

  def getRssUrlForGeotagged: String = {
    siteInformation.getUrl + "/geotagged/rss"
  }

  def getRssUrlForAccepted: String = siteInformation.getUrl + "/accepted/rss"

  def getRssTitleForAccepted: String = siteInformation.getSitename + " - Accepted news items"

  def getRssUrlForJustin: String = {
    siteInformation.getUrl + "/justin/rss"
  }

  def getRssUrlForWatchlist: String = {
    siteInformation.getUrl + "/watchlist/rss"
  }

  def getRssTitleForTag(tag: Tag): String = {
    siteInformation.getSitename + " - " + tag.getDisplayName
  }

  def getRssTitleForPublisher(publisher: Website): String = {
    publisher.title + " RSS Feed"
  }

  def getRssTitleForJustin: String = {
    siteInformation.getSitename + " - Latest Additions"
  }

  def getTitleForWatchlist: String = {
    siteInformation.getSitename + " - News Watchlist"
  }

  def getRssTitleForWatchlist: String = {
    siteInformation.getSitename + " - News watchlist"
  }

  def getRssTitleForTagCombiner(tag: Tag, tag2: Tag): String = {
    siteInformation.getSitename + " - " + tag.getDisplayName + " + " + tag2.getDisplayName
  }

  def getRssUrlForTagCombiner(tag: Tag, tag2: Tag): String = {
    siteInformation.getUrl + "/" + tag.getName + "+" + tag2.getName + "/rss"
  }

  def getRssTitleForPublisherCombiner(publisher: Website, tag: Tag): String = {
    siteInformation.getSitename + " - " + publisher.title + " + " + tag.getDisplayName
  }

  def getRssUrlForPublisherTagCombiner(publisher: Website, tag: Tag): String = {
    siteInformation.getUrl + "/" + publisher.url_words.getOrElse("") + "+" + tag.getName + "/rss"
  }

  def getRssTitleForTagComment(tag: Tag): String = {
    siteInformation.getSitename + " - " + tag.getDisplayName + " comment"
  }

  def getRssUrlForTagComment(tag: Tag): String = {
    siteInformation.getUrl + "/" + tag.getName + "/comment/rss"
  }

  def getRssTitleForTagGeotagged(tag: Tag): String = {
    siteInformation.getSitename + " - " + tag.getDisplayName + " geotagged"
  }

  def getRssUrlForTagGeotagged(tag: Tag): String = {
    siteInformation.getUrl + "/" + tag.getName + "/geotagged/rss"
  }

  def getRssUrlForFeedSuggestions: String = {
    siteInformation.getUrl + "/feeds/inbox/rss"
  }

  def getRssDescriptionForTag(tag: Tag): String = {
    tag.description.getOrElse(siteInformation.getAreaname + " related newsitems tagged with " + tag.getDisplayName)
  }

  def getTitleForSuggestions: String = {
    "Feed newsitem suggestions"
  }

  def getRssTitleForPlace(place: Place, radius: Double): String = {
    var placeLabel: String = place.toString
    if (!Strings.isNullOrEmpty(place.getAddress)) {
      placeLabel = place.getAddress
    }
    else if (place.getLatLong != null) {
      placeLabel = place.getLatLong.toString
    }
    "Newsitems within " + radius + " km of " + placeLabel
  }

  def getRssUrlForPlace(place: Place, radius: Double): String = {
    getRssUrlForPlace(place) + "&radius=" + radius
  }

  private def getRssUrlForPlace(place: Place): String = {
    if (place.getOsmId != null) {
      return getRssUrlForOsmId(place.getOsmId)
    }
    else if (place.getLatLong != null) {
      return getRssUrlForLatLong(place.getLatLong)
    }
    throw new RuntimeException("No location information attached to place")
  }

  def getRssTitleForGeotagged: String = {
    "Geotagged newsitems"
  }

  def getRssHeadingForGivenHeading(heading: String): String = {
    heading + " - " + siteInformation.getSitename
  }

  def getRssUrlForOsmId(osmId: OsmId): String = {
    getRssUrlForGeotagged + "?osm=" + UrlParameterEncoder.encode(osmId.getId + "/" + osmId.getType)
  }

  def getRssUrlForLatLong(latLong: LatLong): String = {
    getRssUrlForGeotagged + "?latitude=" + latLong.getLatitude + "&longitude=" + latLong.getLongitude
  }

}
