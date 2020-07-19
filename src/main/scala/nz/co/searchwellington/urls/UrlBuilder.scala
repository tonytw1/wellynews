package nz.co.searchwellington.urls

import nz.co.searchwellington.controllers.models.helpers.ArchiveMonth
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.frontend.{FrontendFeed, FrontendNewsitem, FrontendResource, FrontendWebsite}
import org.joda.time.{DateTimeZone, Interval}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.dates.DateFormatter
import uk.co.eelpieconsulting.common.geo.model.Place

@Component
class UrlBuilder @Autowired()(siteInformation: SiteInformation, urlWordsGenerator: UrlWordsGenerator)
  extends ArchiveMonth {

  private val dateFormatter = new DateFormatter(DateTimeZone.UTC);

  def getHomeUrl: String = {
    siteInformation.getUrl
  }

  def getImageUrl(filename: String): String = getStaticUrl(filename)

  def getStaticUrl(filename: String): String = {
    siteInformation.getStaticRoot + filename
  }

  def getPublishersAutoCompleteUrl: String = {
    siteInformation.getUrl + "/ajax/publishers"
  }

  def getTagsAutoCompleteUrl: String = {
    siteInformation.getUrl + "/ajax/tags"
  }

  def getFeedUrl(feed: FrontendFeed): String = {
    siteInformation.getUrl + "/feed/" + feed.getUrlWords
  }

  def getFeedUrl(feed: Feed): String = {
    siteInformation.getUrl + "/feed/" + feed.url_words.getOrElse("")
  }

  def getFeedUrlFromFeedName(feedname: String): String = {
    siteInformation.getUrl + "/feed/" + urlWordsGenerator.makeUrlWordsFromName(feedname)
  }

  def getFeedsInboxUrl: String = {
    siteInformation.getUrl + "/feeds/inbox"
  }

  def getFeedsUrl: String = {
    siteInformation.getUrl + "/feeds"
  }

  def getNewFeedForPublisherUrl(publisher: FrontendWebsite): String = {
    "/new-feed?publisher=" + publisher.urlWords
  }

  def getTagUrl(tag: Tag): String = {
    siteInformation.getUrl + "/" + tag.name
  }

  def getTagPageUrl(tag: Tag, page: Long): String = {
    getTagUrl(tag) + "?page=" + page
  }

  def getAutoTagUrl(tag: Tag): String = {
    siteInformation.getUrl + "/" + tag.getName + "/autotag"
  }

  def getTagCombinerUrl(firstTag: Tag, secondTag: Tag): String = {
    siteInformation.getUrl + "/" + firstTag.getName + "+" + secondTag.getName
  }

  def getTagCombinerUrl(firstTag: Tag, secondTag: Tag, page: Int): String = {
    getTagCombinerUrl(firstTag, secondTag) + "?page=" + page
  }

  def getTagSearchUrl(tag: Tag, keywords: String): String = {
    getTagUrl(tag) + "?keywords=" + UrlParameterEncoder.encode(keywords)
  }

  def getLocalPageUrl(resource: FrontendResource): String = {
    resource match {
      case n: FrontendNewsitem =>
        "/newsitem/" + n.id
      case f: FrontendFeed =>
        "/feed/" + f.getUrlWords
      case _ =>
        //siteInformation.getUrl + "/" + resource.getUrlWords
        ""
    }
  }

  def getPublisherUrl(publisher: Website): String = "/" + publisher.url_words.get // TODO Naked get

  def getPublisherPageUrl(publisher: Website, page: Int): String = {
    getPublisherUrl(publisher) + "?page=" + page
  }

  @Deprecated
  def getPublisherUrl(publisherName: String): String = { // TODO use pubslishers url words
    if (publisherName != null) {
      siteInformation.getUrl + "/" + urlWordsGenerator.makeUrlWordsFromName(publisherName)
    } else {
      null
    }
  }

  def getPublisherCombinerUrl(publisher: Website, tag: Tag): String = {
    siteInformation.getUrl + "/" + publisher.url_words.get + "+" + tag.getName // TODO Naked get - make url words mandatory for publishers
  }

  def getPublisherTagCombinerUrl(publisher: FrontendWebsite, tag: Tag): String = {
    siteInformation.getUrl + "/" + publisher.urlWords + "+" + tag.getName
  }

  def getTagCommentUrl(tag: Tag): String = {
    siteInformation.getUrl + "/" + tag.getName + "/comment"
  }

  def getTagGeocodedUrl(tag: Tag): String = {
    siteInformation.getUrl + "/" + tag.getName + "/geotagged"
  }

  def getCommentUrl: String = {
    siteInformation.getUrl + "/comment"
  }

  def getJustinUrl: String = {
    siteInformation.getUrl + "/justin"
  }

  def getGeotaggedUrl: String = {
    siteInformation.getUrl + "/geotagged"
  }

  def getPublicTaggingSubmissionUrl(resource: Resource): String = {
    siteInformation.getUrl + "/tagging/submit"
  }

  @deprecated def getTaggingUrl(resource: FrontendResource): String = {
    this.getLocalPageUrl(resource)
  }

  def getArchiveUrl: String = {
    siteInformation.getUrl + "/archive"
  }

  def getArchiveLinkUrl(link: ArchiveLink): String = {
    "/archive/" + formattedInterval(link.interval)
  }

  def getIntervalUrl(interval: Interval): String = {
    "/archive/" + formattedInterval(interval)
  }

  def getPublisherArchiveLinkUrl(link: PublisherArchiveLink): String = {
    "/" + link.publisher.getUrlWords + "/" + formattedInterval(link.interval)
  }

  def getOpenIDCallbackUrl: String = {
    siteInformation.getUrl + "/openid/callback"
  }

  @deprecated def getProfileUrl(user: User): String = {
    siteInformation.getUrl + "/profiles/" + user.getProfilename
  }

  def getProfileUrlFromProfileName(username: String): String = {
    siteInformation.getUrl + "/profiles/" + UrlParameterEncoder.encode(username)
  }

  def getWatchlistUrl: String = {
    siteInformation.getUrl + "/watchlist"
  }

  def getTwitterCallbackUrl: String = {
    siteInformation.getUrl + "/twitter/callback"
  }

  def getLocationUrlFor(place: Place): String = {
    if (place.getOsmId != null) {
      siteInformation.getUrl + "/geotagged?osm=" + UrlParameterEncoder.encode(place.getOsmId.getId + "/" + place.getOsmId.getType)
    } else if (place.getLatLong != null) {
      siteInformation.getUrl + "/geotagged?latitude=" + place.getLatLong.getLatitude + "&longitude=" + place.getLatLong.getLongitude
    } else {
      null
    }
  }

  def getLocationUrlForRadius(place: Place, radius: Double): String = {
    if (place.getOsmId != null) {
      siteInformation.getUrl + "/geotagged?osm=" + UrlParameterEncoder.encode(place.getOsmId.getId + "/" + place.getOsmId.getType) + "&radius=" + radius
    } else if (place.getLatLong != null) {
      siteInformation.getUrl + "/geotagged?latitude=" + place.getLatLong.getLatitude + "&longitude=" + place.getLatLong.getLongitude + "&radius=" + radius
    } else {
      null
    }
  }

  def getSearchUrlFor(keywords: String, page: Option[Int] = None): String = {
    siteInformation.getUrl + "/search?keywords=" + UrlParameterEncoder.encode(keywords) +
      page.map("&page=" + _).getOrElse("")
  }

  def getTagSearchUrlFor(keywords: String, tag: Tag): String = {
    getTagUrl(tag) + "?keywords=" + UrlParameterEncoder.encode(keywords)
  }

  def getSubmitFeedUrl: String = {
    siteInformation.getUrl + "/new-feed"
  }

  def getSubmitNewsitemUrl: String = {
    siteInformation.getUrl + "/new-newsitem"
  }

  def getSubmitWebsiteUrl: String = {
    siteInformation.getUrl + "/new-website"
  }

  def getResourceUrl(resource: FrontendResource): String = {
    getLocalPageUrl(resource)
  }

  def getOsmWebsiteUrl(osmId: OsmId): String = {
    "http://www.openstreetmap.org/browse/" + osmId.getType.toString.toLowerCase + "/" + osmId.getId
  }

  private def formattedInterval(interval: Interval) = {
    archiveMonthFormat.format(interval.getStart.toDate).toLowerCase
  }

}
