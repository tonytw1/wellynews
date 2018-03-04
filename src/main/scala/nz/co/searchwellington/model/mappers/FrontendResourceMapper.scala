package nz.co.searchwellington.model.mappers

import nz.co.searchwellington.model.frontend._
import nz.co.searchwellington.model._
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import nz.co.searchwellington.views.GeocodeToPlaceMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}

@Component class FrontendResourceMapper @Autowired() (taggingReturnsOfficerService: TaggingReturnsOfficerService, urlWordsGenerator: UrlWordsGenerator,
                                                      geocodeToPlaceMapper: GeocodeToPlaceMapper, mongoRepository: MongoRepository) {

  def createFrontendResourceFrom(contentItem: Resource): FrontendResource = {
    contentItem match {
      case n: Newsitem =>

        val publisher = n.publisher.flatMap { pid =>
          val tenSeconds = Duration(10, SECONDS)
          Await.result(mongoRepository.getResourceById(pid.toInt), tenSeconds)
        }

        FrontendNewsitem(
          id = n.id,
          `type` = n.`type`,
          name = n.title.getOrElse(""),
          url = n.page.getOrElse(null),
          date = n.date2.getOrElse(null),
          acceptedFromFeedName = n.feed.map(f => f.toString).getOrElse(""),
          acceptedByProfilename= n.feed.map(p => p.toString).getOrElse(""),
          accepted = n.accepted2.getOrElse(null),
          image = null,  // TODO
          urlWords = urlWordsGenerator.makeUrlForNewsitem(n).getOrElse(""),
          publisherName = publisher.flatMap(p => p.title).getOrElse("")
        )


      case w: Website =>
        mapFrontendWebsite(w)

      case _ =>
        throw new RuntimeException("Unknown type")
    }
  }
  /*

    if (contentItem.`type` == "F") {
      val frontendFeed = new FrontendFeed
      val contentItemFeed: Feed = contentItem.asInstanceOf[Feed]
      frontendFeed.setLatestItemDate(contentItemFeed.getLatestItemDate)
      frontendContentItem = frontendFeed
    }


    frontendContentItem.setDescription(contentItem.description.getOrElse(""))
    frontendContentItem.setHttpStatus(contentItem.http_status)
    frontendContentItem.setHeld(contentItem.held2)
    contentItem.owner.map { o =>
      frontendContentItem.setOwner(o.toString)
    }
    frontendContentItem.setUrlWords(urlWordsGenerator.makeUrlWordsFromName(contentItem.title.getOrElse("")))
    if (frontendContentItem.getType == "N") {
    }
    else if (frontendContentItem.getType == "F") {
      frontendContentItem.setUrlWords("/feed/" + urlWordsGenerator.makeUrlWordsFromName(contentItem.title.getOrElse("")))
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



    frontendContentItem
    */
 // }

  def mapTagToFrontendTag(tag: Tag): FrontendTag = {
    FrontendTag(id = tag.id, name = tag.getName, displayName = tag.getDisplayName, description = tag.description.getOrElse(null))
  }

  def mapFrontendWebsite(website: Website): FrontendWebsite = {
    FrontendWebsite(name = website.title.getOrElse(""),
      url = website.page.getOrElse(""),
      urlWords = website.url_words.getOrElse(""),
      place = website.geocode.map { g =>
        geocodeToPlaceMapper.mapGeocodeToPlace(g)
      }.getOrElse(null)
    )
  }

}
