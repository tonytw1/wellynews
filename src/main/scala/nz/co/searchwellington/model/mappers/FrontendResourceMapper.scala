package nz.co.searchwellington.model.mappers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.admin.AdminUrlBuilder
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.frontend._
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class FrontendResourceMapper @Autowired()(taggingReturnsOfficerService: TaggingReturnsOfficerService,
                                                     urlWordsGenerator: UrlWordsGenerator,
                                                     mongoRepository: MongoRepository,
                                                     adminUrlBuilder: AdminUrlBuilder) extends ReasonableWaits {

  def createFrontendResourceFrom(contentItem: Resource, loggedInUser: Option[User] = None)(implicit ec: ExecutionContext): Future[FrontendResource] = {
    val eventualIndexTags: Future[Seq[Tag]] = taggingReturnsOfficerService.getIndexTagsForResource(contentItem)
    // TODO this could probably be extracted from the index tags if we wanted to save dao calls
    val eventualHandTags = taggingReturnsOfficerService.getHandTagsForResource(contentItem) // TODO This is interesting as it's applied to unaccepted feed items as well.
    val eventualPlace: Future[Option[Geocode]] = taggingReturnsOfficerService.getIndexGeocodeForResource(contentItem)

    eventualIndexTags.flatMap { indexTags =>
      eventualHandTags.flatMap { handTags =>
        eventualPlace.flatMap { place =>
          mapFrontendResource(contentItem, loggedInUser, handTags, indexTags, place)
        }
      }
    }
  }

  private def mapFrontendResource(contentItem: Resource,
                                  loggedInUser: Option[User],
                                  handTags: Seq[Tag],
                                  indexTags: Seq[Tag],
                                  place: Option[Geocode])(implicit ec: ExecutionContext): Future[FrontendResource] = {
    val eventualFrontendResource = contentItem match {
      case n: Newsitem =>
        val eventualPublisher = n.publisher.map { pid =>
          mongoRepository.getResourceByObjectId(pid)
        }.getOrElse {
          Future.successful(None)
        }

        val eventualAcceptedByUser = n.acceptedBy.map { uid =>
          mongoRepository.getUserByObjectId(uid)
        }.getOrElse {
          Future.successful(None)
        }

        val eventualFeed = n.feed.map { fid =>
          mongoRepository.getResourceByObjectId(fid).flatMap { fo =>
            fo.map { f =>
              createFrontendResourceFrom(f, None).map { r =>
                Some(r.asInstanceOf[FrontendFeed])
              }
            }.getOrElse {
              Future.successful(None)
            }
          }
        }.getOrElse {
          Future.successful(None)
        }

        for {
          feed <- eventualFeed
          publisher <- eventualPublisher
          acceptedByUser <- eventualAcceptedByUser

        } yield {
          FrontendNewsitem(
            id = n.id,
            `type` = n.`type`,
            name = n.title.getOrElse(""),
            url = n.page,
            date = n.date.orNull,
            description = n.description.orNull,
            place = place,
            acceptedFrom = feed,
            acceptedBy = acceptedByUser,
            accepted = n.accepted.orNull,
            image = null, // TODO
            urlWords = urlWordsGenerator.makeUrlForNewsitem(n).getOrElse(""),
            publisher = publisher.map(_.asInstanceOf[Website]), // TODO should be frontend resource or string?
            httpStatus = n.http_status,
            lastScanned = n.last_scanned,
            lastChanged = n.last_changed
          )
        }

      case f: Feed =>
        val eventualFrontendPublisher = f.publisher.map { pid =>
          mongoRepository.getResourceByObjectId(pid).flatMap { po =>
            po.map { p =>
              createFrontendResourceFrom(p, None).map(i => Some(i.asInstanceOf[FrontendWebsite]))
            }.getOrElse {
              Future.successful(None)
            }
          }
        }.getOrElse {
          Future.successful(None)
        }

        for {
          frontendPublisher <- eventualFrontendPublisher

        } yield {
          FrontendFeed(
            id = f.id,
            `type` = f.`type`,
            name = f.title.getOrElse(""),
            url = f.page,
            urlWords = f.url_words.orNull,
            date = f.date.orNull,
            description = f.description.orNull,
            place = place,
            latestItemDate = f.getLatestItemDate,
            lastRead = f.last_read,
            acceptancePolicy = f.acceptance,
            publisher = frontendPublisher,
            httpStatus = f.http_status,
            lastScanned = f.last_scanned,
            lastChanged = f.last_changed
          )
        }

      case l: Watchlist =>
        Future.successful {
          FrontendWatchlist(
            id = l.id,
            `type` = l.`type`,
            name = l.title.getOrElse(""),
            url = l.page,
            date = l.date.orNull,
            description = l.description.orNull,
            place = place,
            httpStatus = l.http_status,
            lastScanned = l.last_scanned,
            lastChanged = l.last_changed
          )
        }

      case w: Website =>
        Future.successful {
          FrontendWebsite(
            id = w.id,
            name = w.title.orNull,
            url = w.page,
            urlWords = w.url_words.orNull,
            description = w.description.getOrElse(""),
            place = w.geocode,
            httpStatus = w.http_status,
            date = w.date.orNull,
            lastScanned = w.last_scanned,
            lastChanged = w.last_changed
          )
        }

      case _ =>
        throw new RuntimeException("Unknown type")
    }

    eventualFrontendResource.map { r =>
      def applyTags(r: FrontendResource): FrontendResource = {
        r match {
          case n: FrontendNewsitem => n.copy(tags = indexTags, handTags = handTags)
          case f: FrontendFeed => f.copy(tags = indexTags, handTags = handTags)
          case l: FrontendWatchlist => l.copy(tags = indexTags, handTags = handTags)
          case w: FrontendWebsite => w.copy(tags = indexTags, handTags = handTags)
        }
      }

      applyTags(applyResourceActions(r, loggedInUser))
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

  private def applyResourceActions(r: FrontendResource, loggedInUser: Option[User]) = {
    loggedInUser.map { l =>
      val availableActions = if (l.admin) {
        val editResourceAction = Action(label = "Edit", link = adminUrlBuilder.getResourceEditUrl(r))
        val checkResourceAction = Action(label = "Check", link = adminUrlBuilder.getResourceCheckUrl(r))
        val deleteResourceAction = Action(label = "Delete", link = adminUrlBuilder.getResourceDeleteUrl(r))
        Seq(editResourceAction, checkResourceAction, deleteResourceAction)
      } else {
        Seq.empty
      }

      r match {
        case n: FrontendNewsitem =>
          n.copy(actions = availableActions)
        case w: FrontendWebsite =>
          w.copy(actions = availableActions)
        case f: FrontendFeed =>
          val withFeedActions = if (l.admin) {
            val acceptAllAction = Action("Accept all", adminUrlBuilder.getAcceptAllFromFeed(f))
            availableActions :+ acceptAllAction
          } else {
            availableActions
          }
          f.copy(actions = withFeedActions)
        case l: FrontendWatchlist =>
          l.copy(actions = availableActions)
      }
    }.getOrElse(r)
  }

}
