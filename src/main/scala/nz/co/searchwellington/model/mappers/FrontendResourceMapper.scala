package nz.co.searchwellington.model.mappers

import nz.co.searchwellington.model._
import nz.co.searchwellington.model.frontend._
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import nz.co.searchwellington.views.GeocodeToPlaceMapper
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}

@Component class FrontendResourceMapper @Autowired() (taggingReturnsOfficerService: TaggingReturnsOfficerService, urlWordsGenerator: UrlWordsGenerator,
                                                      geocodeToPlaceMapper: GeocodeToPlaceMapper, mongoRepository: MongoRepository) {

  private val log = Logger.getLogger(classOf[FrontendResourceMapper])
  private val tenSeconds = Duration(10, SECONDS)

  def createFrontendResourceFrom(contentItem: Resource): FrontendResource = {
    val place = taggingReturnsOfficerService.getIndexGeocodeForResource(contentItem).map(geocodeToPlaceMapper.mapGeocodeToPlace)

    contentItem match {
      case n: Newsitem =>
        val publisher = n.publisher.flatMap { pid =>
          Await.result(mongoRepository.getResourceByObjectId(pid), tenSeconds)
        }

        val feed: Option[FrontendFeed] = n.feed.flatMap { fid =>
          Await.result(mongoRepository.getResourceByObjectId(fid), tenSeconds).map { f =>
            createFrontendResourceFrom(f).asInstanceOf[FrontendFeed]
          }
        }

        val handTags = taggingReturnsOfficerService.getHandTagsForResource(contentItem).
        map(mapTagToFrontendTag).toSeq

        val acceptedByUser = n.acceptedBy.flatMap { uid =>
          Await.result(mongoRepository.getUserByObjectId(uid), tenSeconds)
        }

        FrontendNewsitem(
          id = n.id,
          `type` = n.`type`,
          name = n.title.getOrElse(""),
          url = n.page.getOrElse(null),
          date = n.date.getOrElse(null),
          description = n.description.getOrElse(null),
          place = place,
          acceptedFrom = feed,
          acceptedBy = acceptedByUser,
          accepted = n.accepted.getOrElse(null),
          image = null,  // TODO
          urlWords = urlWordsGenerator.makeUrlForNewsitem(n).getOrElse(""),
          publisher = publisher.map(_.asInstanceOf[Website]),
          tags = frontendTagsFor(n).asJava,
          handTags = handTags.asJava,
          httpStatus = n.http_status
        )

      case f: Feed =>
        val publisher = f.publisher.flatMap { pid =>
          Await.result(mongoRepository.getResourceByObjectId(pid), tenSeconds)
        }

        val frontendPublisher = publisher.map(p => createFrontendResourceFrom(p).asInstanceOf[FrontendWebsite])

        FrontendFeed(
          id = f.id,
          `type` = f.`type`,
          name = f.title.getOrElse(""),
          url = f.page.getOrElse(null),
          urlWords = f.url_words.getOrElse(null),
          date = f.date.getOrElse(null),
          description = f.description.getOrElse(null),
          place = place,
          latestItemDate = f.getLatestItemDate,
          tags = frontendTagsFor(f).asJava,
          lastRead = f.last_read,
          acceptancePolicy = f.acceptance,
          publisher = frontendPublisher,
          httpStatus = f.http_status
        )

      case l: Watchlist =>
        FrontendWatchlist(
          id = l.id,
          `type` = l.`type`,
          name = l.title.getOrElse(""),
          url = l.page.getOrElse(null),
          date = l.date.getOrElse(null),
          description = l.description.getOrElse(null),
          place = place,
          tags = frontendTagsFor(l).asJava,
          httpStatus = l.http_status
        )

      case w: Website =>
        mapFrontendWebsite(w)

      case _ =>
        throw new RuntimeException("Unknown type")
    }
  }
  /*


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




    frontendContentItem
    */
 // }

  def mapFrontendWebsite(website: Website): FrontendWebsite = {
    FrontendWebsite(
      id = website.id,
      name = website.title.orNull,
      url = website.page.orNull,
      urlWords = website.url_words.orNull,
      description = website.description.getOrElse(""),
      place = website.geocode.map { g =>
        geocodeToPlaceMapper.mapGeocodeToPlace(g)
      },
      tags = frontendTagsFor(website).asJava,
      httpStatus = website.http_status,
      date = website.date.orNull
    )
  }

  def mapTagToFrontendTag(tag: Tag): FrontendTag = {
    FrontendTag(id = tag.id, name = tag.getName, displayName = tag.getDisplayName, description = tag.description.getOrElse(null))
  }

  private def frontendTagsFor(resource: Resource): Seq[FrontendTag] = {
    taggingReturnsOfficerService.getHandTagsForResource(resource).map { tag =>
      FrontendTag(id = tag._id.stringify, name = tag.name, displayName = tag.display_name, description = tag.description.orNull)
    }.toSeq
  }

}
