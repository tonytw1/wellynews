package nz.co.searchwellington.urls

import nz.co.searchwellington.controllers.models.helpers.ArchiveMonth
import nz.co.searchwellington.filters.attributesetters.PageParameterFilter
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.frontend.{FrontendFeed, FrontendNewsitem, FrontendResource, FrontendWebsite}
import nz.co.searchwellington.model.geo.OsmId
import org.joda.time.Interval
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.geo.model.Place

import java.time.LocalDate

@Component
class UrlBuilder @Autowired()(siteInformation: SiteInformation, urlWordsGenerator: UrlWordsGenerator) extends ArchiveMonth {

  def fullyQualified(uri: String): String = {
    siteInformation.getUrl + uri
  }

  def getHomeUri: String = "/"

  def getImageUrl(filename: String): String = getStaticUrl(filename)

  def getStaticUrl(filename: String): String = {
    siteInformation.getStaticRoot + filename
  }

  def getPublishersUrl: String = {
    getHomeUri + "publishers"
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

  def getTagsUrl(): String = "/tags"

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
        null
    }
  }

  def getLocalPageUrl(resource: Resource): String = {
    resource match {
      case n: FrontendNewsitem =>
        "/newsitem/" + n.id
      case f: FrontendFeed =>
        "/feed/" + f.getUrlWords
      case w: FrontendWebsite =>
        "/" + w.urlWords
      case _ =>
        ""  // TODO Watchlist items have no local page
    }
  }

  def getPublisherUrl(publisher: Website): String = "/" + publisher.url_words.get // TODO Naked get
  def getPublisherUrl(publisher: FrontendWebsite): String = "/" + publisher.urlWords

  def getPublisherPageUrl(publisher: Website, page: Int): String = {
    getPublisherUrl(publisher) + "?page=" + page
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

  def getAcceptedUrl: String = "/accepted"

  def getAcceptedUrl(date: LocalDate): String = "/accepted?date=" + date.toString

  def getJustinUrl: String = {
    "/justin"
  }

  def getGeotaggedUrl: String = {
    "/geotagged"
  }

  def getArchiveUrl: String = {
    "/archive"
  }

  def getArchiveLinkUrl(link: IntervalLink): String = {
    link match {
      case p: PublisherArchiveLink =>
        getPublisherArchiveLinkUrl(p)
      case t: TagArchiveLink =>
        getTagArchiveLinkUrl(t)
      case _ =>
        "/archive/" + renderYearMonth(link.interval)
    }
  }

  def getIntervalUrl(interval: Interval): String = {
    "/archive/" + renderYearMonth(interval)
  }

  def getPublisherArchiveLinkUrl(link: PublisherArchiveLink): String = {
    "/" + link.publisher.getUrlWords + "/" + renderYearMonth(link.interval)
  }

  def getTagArchiveLinkUrl(link: TagArchiveLink): String = {
     getTagUrl(link.tag) + "/" + renderYearMonth(link.interval)
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

  def getSearchUrlFor(q: String): String = getSearchUrlFor(q, None, None)

  def getSearchUrlFor(keywords: String, page: Option[Int] = None, tag: Option[Tag] = None, publisher: Option[Website] = None): String = {
    val ps = Seq(
      page.map(p => PageParameterFilter.PAGE_ATTRIBUTE -> p.toString),
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
    "http://www.openstreetmap.org/browse/" + osmId.getType.toLowerCase + "/" + osmId.getId
  }

  def getDiscoveredFeeds: String = getFeedsUrl + "/discovered"

}
