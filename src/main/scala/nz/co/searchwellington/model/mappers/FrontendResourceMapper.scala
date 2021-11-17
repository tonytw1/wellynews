package nz.co.searchwellington.model.mappers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.admin.AdminUrlBuilder
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.frontend._
import nz.co.searchwellington.repositories.HandTaggingDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.{IndexTagsService, ResourceTagging}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class FrontendResourceMapper @Autowired()(indexTagsService: IndexTagsService,
                                                     mongoRepository: MongoRepository,
                                                     adminUrlBuilder: AdminUrlBuilder,
                                                     val handTaggingDAO: HandTaggingDAO)
  extends ReasonableWaits with ResourceTagging {

  def createFrontendResourceFrom(contentItem: Resource, loggedInUser: Option[User] = None)(implicit ec: ExecutionContext): Future[FrontendResource] = {
    val eventualHandTags = getHandTagsForResource(contentItem) // TODO This is interesting as it's applied to unaccepted feed items as well.
    val eventualIndexTags = indexTagsService.getIndexTagsForResource(contentItem) // TODO this could use the above handtaggings to save a duplicate query
    val eventualPlace = indexTagsService.getIndexGeocodeForResource(contentItem)
    (for {
      indexTags <- eventualIndexTags
      handTags <- eventualHandTags
      place <- eventualPlace
    } yield {
      mapFrontendResource(contentItem, place).map { frontendResource =>
        val actions = actionsFor(frontendResource, loggedInUser)
        frontendResource match {
          // TODO this match to call the same code on each class is a weird smell
          case n: FrontendNewsitem => n.copy(tags = indexTags, handTags = handTags, actions = actions)
          case f: FrontendFeed => f.copy(tags = indexTags, handTags = handTags, actions = actions)
          case l: FrontendWatchlist => l.copy(tags = indexTags, handTags = handTags, actions = actions)
          case w: FrontendWebsite => w.copy(tags = indexTags, handTags = handTags, actions = actions)
        }
      }
    }).flatten
  }

  def mapFrontendResource(contentItem: Resource, place: Option[Geocode])(implicit ec: ExecutionContext): Future[FrontendResource] = {
    contentItem match {
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
        val eventualOwner = n.owner.map { uid =>
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
          owner <- eventualOwner
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
            urlWords = n.url_words.orNull,
            publisherName = publisher.flatMap(_.title),
            publisherUrlWords = publisher.flatMap(_.url_words),
            httpStatus = n.http_status,
            lastScanned = n.last_scanned,
            lastChanged = n.last_changed,
            owner = owner.map(user => user.profilename.getOrElse(user._id.stringify)).orNull
          )
        }

      case f: Feed =>
        val eventualPublisher = f.publisher.map { pid =>
          mongoRepository.getResourceByObjectId(pid)
        }.getOrElse {
          Future.successful(None)
        }
        val eventualOwner = f.owner.map { uid =>
          mongoRepository.getUserByObjectId(uid)
        }.getOrElse {
          Future.successful(None)
        }
        for {
          publisher <- eventualPublisher
          owner <- eventualOwner
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
            publisherName = publisher.flatMap(_.title),
            publisherUrlWords = publisher.flatMap(_.url_words),
            httpStatus = f.http_status,
            lastScanned = f.last_scanned,
            lastChanged = f.last_changed,
            owner = owner.map(user => user.profilename.getOrElse(user._id.stringify)).orNull
          )
        }

      case l: Watchlist =>
        val eventualPublisher = l.publisher.map { pid =>
          mongoRepository.getResourceByObjectId(pid)
        }.getOrElse {
          Future.successful(None)
        }
        val eventualOwner = l.owner.map { uid =>
          mongoRepository.getUserByObjectId(uid)
        }.getOrElse {
          Future.successful(None)
        }

        for {
          publisher <- eventualPublisher
          owner <- eventualOwner
        } yield {
          FrontendWatchlist(
            id = l.id,
            `type` = l.`type`,
            name = l.title.getOrElse(""),
            url = l.page,
            date = l.date.orNull,
            publisherName = publisher.flatMap(_.title),
            publisherUrlWords = publisher.flatMap(_.url_words),
            description = l.description.orNull,
            place = place,
            httpStatus = l.http_status,
            lastScanned = l.last_scanned,
            lastChanged = l.last_changed,
            owner = owner.map(user => user.profilename.getOrElse(user._id.stringify)).orNull
          )
        }

      case w: Website =>
        val eventualOwner = w.owner.map { uid =>
          mongoRepository.getUserByObjectId(uid)
        }.getOrElse {
          Future.successful(None)
        }

        for {
          owner <- eventualOwner
        } yield {

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
            lastChanged = w.last_changed,
            owner = owner.map(user => user.profilename.getOrElse(user._id.stringify)).orNull
          )
        }

      case _ =>
        throw new RuntimeException("Unknown type")
    }
  }

  private def actionsFor(r: FrontendResource, loggedInUser: Option[User]): Seq[Action] = {
    loggedInUser.map { l =>
      if (l.admin) {
        val editResourceAction = Action(label = "Edit", link = adminUrlBuilder.getResourceEditUrl(r))
        val checkResourceAction = Action(label = "Check", link = adminUrlBuilder.getResourceCheckUrl(r))
        val deleteResourceAction = Action(label = "Delete", link = adminUrlBuilder.getResourceDeleteUrl(r))
        val baseActions = Seq(editResourceAction, checkResourceAction, deleteResourceAction)

        r match {
          case f: FrontendFeed =>
            val acceptAllAction = Action("Accept all", adminUrlBuilder.getAcceptAllFromFeed(f))
            baseActions :+ acceptAllAction
          case _ => baseActions
        }

      } else {
        Seq.empty
      }
    }.getOrElse {
      Seq.empty
    }
  }

}
