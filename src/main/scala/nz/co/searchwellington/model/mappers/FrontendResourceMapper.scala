package nz.co.searchwellington.model.mappers

import java.util.List

import nz.co.searchwellington.model.{Feed, Geocode, Newsitem, Resource, Tag, UrlWordsGenerator}
import nz.co.searchwellington.model.frontend.{FrontendFeed, FrontendImage, FrontendNewsitem, FrontendResource, FrontendTag}
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import nz.co.searchwellington.views.GeocodeToPlaceMapper
import org.elasticsearch.common.collect.Lists
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.mutable

import scala.collection.JavaConversions._
import scala.collection.JavaConversions._

@Component class FrontendResourceMapper @Autowired() (taggingReturnsOfficerService: TaggingReturnsOfficerService, urlWordsGenerator: UrlWordsGenerator, geocodeToPlaceMapper: GeocodeToPlaceMapper) {

  def createFrontendResourceFrom(contentItem: Resource): FrontendResource = {
    var frontendContentItem: FrontendResource = new FrontendResource
    if (contentItem.getType == "N") {
      val contentItemNewsitem: Newsitem = contentItem.asInstanceOf[Newsitem]
      val frontendNewsitem: FrontendNewsitem = new FrontendNewsitem
      frontendNewsitem.setPublisherName(contentItemNewsitem.getPublisherName)
      frontendNewsitem.setAcceptedFromFeedName(if (contentItemNewsitem.getFeed != null) contentItemNewsitem.getFeed.getName else null)
      frontendNewsitem.setAcceptedByProfilename(if (contentItemNewsitem.getAcceptedBy != null) contentItemNewsitem.getAcceptedBy.getProfilename else null)
      frontendNewsitem.setAccepted(contentItemNewsitem.getAccepted)
      if (contentItemNewsitem.getImage != null) {
        frontendNewsitem.setFrontendImage(new FrontendImage(contentItemNewsitem.getImage.getUrl))
      }
      frontendContentItem = frontendNewsitem
    }
    if (contentItem.getType == "F") {
      val frontendFeed: FrontendFeed = new FrontendFeed
      val contentItemFeed: Feed = contentItem.asInstanceOf[Feed]
      frontendFeed.setPublisherName(contentItemFeed.getPublisherName)
      frontendFeed.setLatestItemDate(contentItemFeed.getLatestItemDate)
      frontendContentItem = frontendFeed
    }
    frontendContentItem.setId(contentItem.getId)
    frontendContentItem.setType(contentItem.getType)
    frontendContentItem.setName(contentItem.getName)
    frontendContentItem.setUrl(contentItem.getUrl)
    frontendContentItem.setDate(contentItem.getDate)
    frontendContentItem.setDescription(contentItem.getDescription)
    frontendContentItem.setHttpStatus(contentItem.getHttpStatus)
    frontendContentItem.setHeld(contentItem.isHeld)
    if (contentItem.getOwner != null) {
      frontendContentItem.setOwner(contentItem.getOwner.getProfilename)
    }
    frontendContentItem.setUrlWords(urlWordsGenerator.makeUrlWordsFromName(contentItem.getName))
    if (frontendContentItem.getType == "N") {
      frontendContentItem.setUrlWords(urlWordsGenerator.makeUrlForNewsitem(frontendContentItem.asInstanceOf[FrontendNewsitem]))
    }
    else if (frontendContentItem.getType == "F") {
      frontendContentItem.setUrlWords("/feed/" + urlWordsGenerator.makeUrlWordsFromName(contentItem.getName))
    }

    val tags: mutable.MutableList[FrontendTag] = mutable.MutableList.empty
    for (tag <- taggingReturnsOfficerService.getIndexTagsForResource(contentItem)) {
      tags.add(mapTagToFrontendTag(tag))
    }
    frontendContentItem.setTags(tags)

    val handTags: mutable.MutableList[FrontendTag] = mutable.MutableList.empty
    for (tag <- taggingReturnsOfficerService.getHandTagsForResource(contentItem)) {
      handTags.add(mapTagToFrontendTag(tag))
    }
    frontendContentItem.setHandTags(handTags)

    val contentItemGeocode: Geocode = taggingReturnsOfficerService.getIndexGeocodeForResource(contentItem)
    if (contentItemGeocode != null) {
      frontendContentItem.setPlace(geocodeToPlaceMapper.mapGeocodeToPlace(contentItemGeocode))
    }
    return frontendContentItem
  }

  private def mapTagToFrontendTag(tag: Tag): FrontendTag = {
    return new FrontendTag(tag.getName, tag.getDisplayName)
  }

}