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
class UrlBuilder @Autowired()(siteInformation: SiteInformation, urlWordsGenerator: UrlWordsGenerator) extends ArchiveMonth {

  def fullyQualified(uri: String): String = {
    siteInformation.getUrl + uri
  }

  def getHomeUrl: String = "/"

  def getImageUrl(filename: String): String = getStaticUrl(filename)

  def getStaticUrl(filename: String): String = {
    siteInformation.getStaticRoot + filename
  }

  def getPublishersAutoCompleteUrl: String = {
    "/ajax/publishers"
  }

  def getTagsAutoCompleteUrl: String = {
    "/ajax/tags"
  }

  def getFeedUrl(feed: FrontendFeed): String = {
    "/feed/" + feed.getUrlWords
  }

  def getFeedUrl(feed: Feed): String = {
    "/feed/" + feed.url_words.getOrElse("")
  }

  def getFeedUrlFromFeedName(feedname: String): String = {
    "/feed/" + urlWordsGenerator.makeUrlWordsFromName(feedname)
  }

  def getFeedsInboxUrl: String = {
    "/feeds/inbox"
  }

  def getFeedsUrl: String = {
    "/feeds"
  }

  def getNewFeedForPublisherUrl(publisher: FrontendWebsite): String = {
    "/new-feed?publisher=" + publisher.urlWords
  }

  def getTagUrl(tag: Tag): String = {
    "/" + tag.name
  }

  def getTagPageUrl(tag: Tag, page: Long): String = {
    getTagUrl(tag) + "?page=" + page
  }

  def getAutoTagUrl(tag: Tag): String = {
    "/" + tag.getName + "/autotag"
  }

  def getTagCombinerUrl(firstTag: Tag, secondTag: Tag): String = {
    "/" + firstTag.getName + "+" + secondTag.getName
  }

  def getTagCombinerUrl(firstTag: Tag, secondTag: Tag, page: Int): String = {
    getTagCombinerUrl(firstTag, secondTag) + "?page=" + page
  }

  def getLocalPageUrl(resource: FrontendResource): String = {
    resource match {
      case n: FrontendNewsitem =>
        "/newsitem/" + n.id
      case f: FrontendFeed =>
        "/feed/" + f.getUrlWords
      case w: FrontendWebsite =>
        "/" + w.urlWords
      case _ =>
        //"/" + resource.getUrlWords
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
      "/" + urlWordsGenerator.makeUrlWordsFromName(publisherName)
    } else {
      null
    }
  }

  def getPublisherCombinerUrl(publisher: Website, tag: Tag): String = {
    "/" + publisher.url_words.get + "+" + tag.getName // TODO Naked get - make url words mandatory for publishers
  }

  def getPublisherTagCombinerUrl(publisher: FrontendWebsite, tag: Tag): String = {
    "/" + publisher.urlWords + "+" + tag.getName
  }

  def getTagCommentUrl(tag: Tag): String = {
    "/" + tag.getName + "/comment"
  }

  def getTagGeocodedUrl(tag: Tag): String = {
    "/" + tag.getName + "/geotagged"
  }

  def getCommentUrl: String = {
    "/comment"
  }

  def getJustinUrl: String = {
    "/justin"
  }

  def getGeotaggedUrl: String = {
    "/geotagged"
  }

  def getPublicTaggingSubmissionUrl(resource: Resource): String = {
    "/tagging/submit"
  }

  def getArchiveUrl: String = {
    "/archive"
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
    "/openid/callback"
  }

  def getProfileUrl(user: User): String = {
    getProfileUrlFromProfileName(user.getProfilename)
  }

  def getProfileUrlFromProfileName(username: String): String = {
    "/profiles/" + UrlParameterEncoder.encode(username)
  }

  def getWatchlistUrl: String = {
    "/watchlist"
  }

  def getTwitterCallbackUrl: String = siteInformation.getUrl + "/twitter/callback"

  def getLocationUrlFor(place: Place): String = {
    if (place.getOsmId != null) {
      "/geotagged?osm=" + UrlParameterEncoder.encode(place.getOsmId.getId + "/" + place.getOsmId.getType)
    } else if (place.getLatLong != null) {
      "/geotagged?latitude=" + place.getLatLong.getLatitude + "&longitude=" + place.getLatLong.getLongitude
    } else {
      null
    }
  }

  def getLocationUrlForRadius(place: Place, radius: Double): String = {
    if (place.getOsmId != null) {
      "/geotagged?osm=" + UrlParameterEncoder.encode(place.getOsmId.getId + "/" + place.getOsmId.getType) + "&radius=" + radius
    } else if (place.getLatLong != null) {
      "/geotagged?latitude=" + place.getLatLong.getLatitude + "&longitude=" + place.getLatLong.getLongitude + "&radius=" + radius
    } else {
      null
    }
  }

  def getSearchUrlFor(keywords: String, page: Option[Int] = None, tag: Option[Tag] = None, publisher: Option[Website] = None): String = {
    val ps = Seq(
      page.map(p => "page" -> p.toString),
      tag.map(t => "tag" -> t.name),
      publisher.map(p => "publisher" -> p.url_words.get)  // TODO naked get
    ).flatten

    io.lemonlabs.uri.Url(path = "/search").addParam("q", keywords).addParams(ps).toRelativeUrl.toString()
  }

  def getTagSearchUrl(tag: Tag, keywords: String): String = {
    getSearchUrlFor(keywords, tag = Some(tag))
  }

  def getPublisherSearchUrl(publisher: Website, keywords: String): String = {
    getSearchUrlFor(keywords, publisher = Some(publisher))
  }

  def getSubmitFeedUrl: String = {
    "/new-feed"
  }

  def getSubmitNewsitemUrl: String = {
    "/new-newsitem"
  }

  def getSubmitWatchlistUrl: String = "/new-watchlist"

  def getSubmitWebsiteUrl: String = {
    "/new-website"
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

  def getDiscoveredFeeds(): String = {
    getFeedsUrl + "/discovered"
  }

}
