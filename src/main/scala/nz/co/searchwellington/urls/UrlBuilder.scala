package nz.co.searchwellington.urls

import java.util.Date

import nz.co.searchwellington.model._
import nz.co.searchwellington.model.frontend.{FrontendFeed, FrontendResource, FrontendTag}
import org.joda.time.DateTimeZone
import org.springframework.beans.factory.annotation.Autowired
import uk.co.eelpieconsulting.common.dates.DateFormatter
import uk.co.eelpieconsulting.common.geo.model.{OsmId, Place}
import org.springframework.stereotype.Component

@Component
class UrlBuilder @Autowired() (siteInformation: SiteInformation, urlWordsGenerator: UrlWordsGenerator) {

  private val dateFormatter = new DateFormatter(DateTimeZone.UTC);

  def getHomeUrl: String = {
    return siteInformation.getUrl
  }

  def getImageUrl(filename: String): String = {
    return siteInformation.getImageRoot + filename
  }

  def getStaticUrl(filename: String): String = {
    return siteInformation.getStaticRoot + filename
  }

  def getPublishersAutoCompleteUrl: String = {
    return siteInformation.getUrl + "/ajax/publishers"
  }

  def getTagsAutoCompleteUrl: String = {
    return siteInformation.getUrl + "/ajax/tags"
  }

  def getFeedUrl(feed: FrontendFeed): String = {
    return siteInformation.getUrl + "/feed/" + feed.getUrlWords
  }

  def getFeedUrl(feed: Feed): String = {
    return siteInformation.getUrl + "/feed/" + feed.getUrlWords
  }

  def getFeedUrlFromFeedName(feedname: String): String = {
    return siteInformation.getUrl + "/feed/" + urlWordsGenerator.makeUrlWordsFromName(feedname)
  }

  def getFeedsInboxUrl: String = {
    return siteInformation.getUrl + "/feeds/inbox"
  }

  def getFeedsUrl: String = {
    return siteInformation.getUrl + "/feeds"
  }

  def getTagUrl(tag: Tag): String = {
    return siteInformation.getUrl + "/" + tag.getName
  }

  def getTagUrl(tag: FrontendTag): String = {
    return siteInformation.getUrl + "/" + tag.getId
  }

  def getAutoTagUrl(tag: Tag): String = {
    return siteInformation.getUrl + "/" + tag.getName + "/autotag"
  }

  def getTagCombinerUrl(firstTag: Tag, secondTag: FrontendTag): String = {
    return siteInformation.getUrl + "/" + firstTag.getName + "+" + secondTag.getId
  }

  def getTagCombinerUrl(firstTag: Tag, secondTag: Tag): String = {
    return siteInformation.getUrl + "/" + firstTag.getName + "+" + secondTag.getName
  }

  def getTagSearchUrl(tag: Tag, keywords: String): String = {
    return getTagUrl(tag) + "?keywords=" + UrlParameterEncoder.encode(keywords)
  }

  def getLocalPageUrl(resource: FrontendResource): String = {
    return siteInformation.getUrl + "/" + resource.getUrlWords
  }

  def getPublisherUrl(publisherName: String): String = {
    if (publisherName != null) {
      return siteInformation.getUrl + "/" + urlWordsGenerator.makeUrlWordsFromName(publisherName)
    }
    return null
  }

  def getPublisherCombinerUrl(publisherName: String, tag: Tag): String = {
    return siteInformation.getUrl + "/" + urlWordsGenerator.makeUrlWordsFromName(publisherName) + "+" + tag.getName
  }

  def getTagCommentUrl(tag: Tag): String = {
    return siteInformation.getUrl + "/" + tag.getName + "/comment"
  }

  def getTagGeocodedUrl(tag: Tag): String = {
    return siteInformation.getUrl + "/" + tag.getName + "/geotagged"
  }

  def getCommentUrl: String = {
    return siteInformation.getUrl + "/comment"
  }

  def getJustinUrl: String = {
    return siteInformation.getUrl + "/justin"
  }

  def getGeotaggedUrl: String = {
    return siteInformation.getUrl + "/geotagged"
  }

  def getPublicTaggingSubmissionUrl(resource: Resource): String = {
    return siteInformation.getUrl + "/tagging/submit"
  }

  @deprecated def getTaggingUrl(resource: FrontendResource): String = {
    return this.getLocalPageUrl(resource)
  }

  def getArchiveUrl: String = {
    return siteInformation.getUrl + "/archive"
  }

  def getArchiveLinkUrl(date: Date): String = {
    return siteInformation.getUrl + "/archive/" + dateFormatter.yearMonthUrlStub(date)
  }

  def getOpenIDCallbackUrl: String = {
    return siteInformation.getUrl + "/openid/callback"
  }

  @deprecated def getProfileUrl(user: User): String = {
    return siteInformation.getUrl + "/profiles/" + user.getProfilename
  }

  def getProfileUrlFromProfileName(username: String): String = {
    return siteInformation.getUrl + "/profiles/" + UrlParameterEncoder.encode(username)
  }

  def getWatchlistUrl: String = {
    return siteInformation.getUrl + "/watchlist"
  }

  def getTwitterCallbackUrl: String = {
    return siteInformation.getUrl + "/twitter/callback"
  }

  def getLocationUrlFor(place: Place): String = {
    if (place.getOsmId != null) {
      return siteInformation.getUrl + "/geotagged?osm=" + UrlParameterEncoder.encode(place.getOsmId.getId + "/" + place.getOsmId.getType)
    }
    if (place.getLatLong != null) {
      return siteInformation.getUrl + "/geotagged?latitude=" + place.getLatLong.getLatitude + "&longitude=" + place.getLatLong.getLongitude
    }
    return null
  }

  def getLocationUrlForRadius(place: Place, radius: Double): String = {
    if (place.getOsmId != null) {
      return siteInformation.getUrl + "/geotagged?osm=" + UrlParameterEncoder.encode(place.getOsmId.getId + "/" + place.getOsmId.getType) + "&radius=" + radius
    }
    if (place.getLatLong != null) {
      return siteInformation.getUrl + "/geotagged?latitude=" + place.getLatLong.getLatitude + "&longitude=" + place.getLatLong.getLongitude + "&radius=" + radius
    }
    return null
  }

  def getSearchUrlFor(keywords: String): String = {
    return siteInformation.getUrl + "/search?keywords=" + UrlParameterEncoder.encode(keywords)
  }

  def getTagSearchUrlFor(keywords: String, tag: Tag): String = {
    return getTagUrl(tag) + "?keywords=" + UrlParameterEncoder.encode(keywords)
  }

  def getSubmitWebsiteUrl: String = {
    return siteInformation.getUrl + "/edit/submit/website"
  }

  def getSubmitNewsitemUrl: String = {
    return siteInformation.getUrl + "/edit/submit/newsitem"
  }

  def getSubmitFeedUrl: String = {
    return siteInformation.getUrl + "/edit/submit/feed"
  }

  def getResourceUrl(resource: FrontendResource): String = {
    return getLocalPageUrl(resource)
  }

  def getOsmWebsiteUrl(osmId: OsmId): String = {
    return "http://www.openstreetmap.org/browse/" + osmId.getType.toString.toLowerCase + "/" + osmId.getId
  }

}
