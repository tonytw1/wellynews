package nz.co.searchwellington.model.mappers

import nz.co.searchwellington.model.frontend._
import nz.co.searchwellington.model._
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import nz.co.searchwellington.views.GeocodeToPlaceMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._
import scala.collection.mutable

@Component class FrontendResourceMapper @Autowired() (taggingReturnsOfficerService: TaggingReturnsOfficerService, urlWordsGenerator: UrlWordsGenerator, geocodeToPlaceMapper: GeocodeToPlaceMapper) {

  def createFrontendResourceFrom(contentItem: Resource): FrontendResource = {
    var frontendContentItem = new FrontendResource
    if (contentItem.getType == "N") {
      val contentItemNewsitem = contentItem.asInstanceOf[Newsitem]

      val frontendNewsitem = new FrontendNewsitem
      // frontendNewsitem.setAcceptedFromFeedName(if (contentItemNewsitem.getFeed != null) contentItemNewsitem.getFeed.getName else null)
      // frontendNewsitem.setAcceptedByProfilename(if (contentItemNewsitem.getAcceptedBy != null) contentItemNewsitem.getAcceptedBy.getProfilename else null)
      frontendNewsitem.setAccepted(contentItemNewsitem.getAccepted)

      if (contentItemNewsitem.getImage != null) {
        // frontendNewsitem.setFrontendImage(new FrontendImage(contentItemNewsitem.getImage.getUrl))
      }
      frontendContentItem = frontendNewsitem
    }

    if (contentItem.getType == "F") {
      val frontendFeed = new FrontendFeed
      val contentItemFeed: Feed = contentItem.asInstanceOf[Feed]
      frontendFeed.setLatestItemDate(contentItemFeed.getLatestItemDate)
      frontendContentItem = frontendFeed
    }

    frontendContentItem.setId(contentItem.getId)
    frontendContentItem.setType(contentItem.getType)
    frontendContentItem.setName(contentItem.getName)
    frontendContentItem.setUrl(contentItem.getUrl)

    frontendContentItem.setDate(contentItem.date2.getOrElse(null))

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
      tags += mapTagToFrontendTag(tag)
    }
    frontendContentItem.setTags(tags)

    val handTags: mutable.MutableList[FrontendTag] = mutable.MutableList.empty
    for (tag <- taggingReturnsOfficerService.getHandTagsForResource(contentItem)) {
      handTags += mapTagToFrontendTag(tag)
    }
    frontendContentItem.setHandTags(handTags)

    val contentItemGeocode: Geocode = taggingReturnsOfficerService.getIndexGeocodeForResource(contentItem)
    if (contentItemGeocode != null) {
      frontendContentItem.setPlace(geocodeToPlaceMapper.mapGeocodeToPlace(contentItemGeocode))
    }

    contentItem match {
      case p: PublishedResource => p.publisher.map(p => frontendContentItem.setPublisherName(p.toString))
    }


    frontendContentItem
  }

  def mapTagToFrontendTag(tag: Tag): FrontendTag = {
    return new FrontendTag(tag.getName, tag.getDisplayName)
  }

  def mapFrontendWebsite(website: Website): FrontendWebsite = {
    // TODO why is this different from the above?
    val frontendPublisher = new FrontendWebsite
    frontendPublisher.setName(website.getName)
    frontendPublisher.setUrlWords(website.getUrlWords)
    frontendPublisher.setUrl(website.getUrl)
    if (website.getGeocode != null) {
      frontendPublisher.setPlace(geocodeToPlaceMapper.mapGeocodeToPlace(website.getGeocode))
    }
    frontendPublisher
  }

}
