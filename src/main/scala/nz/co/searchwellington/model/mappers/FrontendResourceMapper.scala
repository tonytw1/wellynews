package nz.co.searchwellington.model.mappers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.frontend._
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.{Duration, SECONDS}

@Component class FrontendResourceMapper @Autowired()(taggingReturnsOfficerService: TaggingReturnsOfficerService, urlWordsGenerator: UrlWordsGenerator,
                                                     mongoRepository: MongoRepository) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[FrontendResourceMapper])

  def createFrontendResourceFrom(contentItem: Resource)(implicit ec: ExecutionContext): FrontendResource = {
    val place = Await.result(taggingReturnsOfficerService.getIndexGeocodeForResource(contentItem), TenSeconds)

    contentItem match {
      case n: Newsitem =>
        val publisher = n.publisher.flatMap { pid =>
          Await.result(mongoRepository.getResourceByObjectId(pid), TenSeconds)
        }

        val feed: Option[FrontendFeed] = n.feed.flatMap { fid =>
          Await.result(mongoRepository.getResourceByObjectId(fid), TenSeconds).map { f =>
            createFrontendResourceFrom(f).asInstanceOf[FrontendFeed]
          }
        }

        val handTags = Await.result(taggingReturnsOfficerService.getHandTagsForResource(contentItem), TenSeconds)
        val acceptedByUser = n.acceptedBy.flatMap { uid =>
          Await.result(mongoRepository.getUserByObjectId(uid), TenSeconds)
        }

        FrontendNewsitem(
          id = n.id,
          `type` = n.`type`,
          name = n.title.getOrElse(""),
          url = n.page.orNull, // TODO push to the getters
          date = n.date.orNull,
          description = n.description.orNull,
          place = place,
          acceptedFrom = feed,
          acceptedBy = acceptedByUser,
          accepted = n.accepted.orNull,
          image = null, // TODO
          urlWords = urlWordsGenerator.makeUrlForNewsitem(n).getOrElse(""),
          publisher = publisher.map(_.asInstanceOf[Website]),
          tags = frontendTagsFor(n),
          handTags = handTags,
          httpStatus = n.http_status
        )

      case f: Feed =>
        val publisher = f.publisher.flatMap { pid =>
          Await.result(mongoRepository.getResourceByObjectId(pid), TenSeconds)
        }

        val frontendPublisher = publisher.map(p => createFrontendResourceFrom(p).asInstanceOf[FrontendWebsite])

        FrontendFeed(
          id = f.id,
          `type` = f.`type`,
          name = f.title.getOrElse(""),
          url = f.page.orNull,
          urlWords = f.url_words.orNull,
          date = f.date.orNull,
          description = f.description.orNull,
          place = place,
          latestItemDate = f.getLatestItemDate,
          tags = frontendTagsFor(f),
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
          url = l.page.orNull,
          date = l.date.orNull,
          description = l.description.orNull,
          place = place,
          tags = frontendTagsFor(l),
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
      place = website.geocode,
      tags = frontendTagsFor(website),
      httpStatus = website.http_status,
      date = website.date.orNull
    )
  }

  private def frontendTagsFor(resource: Resource): Seq[Tag] = {
    Await.result(taggingReturnsOfficerService.getHandTagsForResource(resource), TenSeconds)
  }

}
