package nz.co.searchwellington.model.mappers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.admin.AdminUrlBuilder
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.frontend._
import nz.co.searchwellington.model.geo.Geocode
import nz.co.searchwellington.model.taggingvotes.HandTagging
import nz.co.searchwellington.permissions.EditPermissionService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.{IndexTagsService, TaggingReturnsOfficerService}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component class FrontendResourceMapper @Autowired()(indexTagsService: IndexTagsService,
                                                     val mongoRepository: MongoRepository,
                                                     adminUrlBuilder: AdminUrlBuilder,
                                                     taggingReturnsOfficerService: TaggingReturnsOfficerService,
                                                     editPermissionService: EditPermissionService)
  extends ReasonableWaits {

  def createFrontendResourceFrom(contentItem: Resource, loggedInUser: Option[User] = None)(implicit ec: ExecutionContext): Future[FrontendResource] = {
    val eventualTaggingVotes = taggingReturnsOfficerService.getTaggingsVotesForResource(contentItem)
    val eventualPlace = indexTagsService.getIndexGeocodeForResource(contentItem)
    (for {
      taggingVotes <- eventualTaggingVotes
      place <- eventualPlace
    } yield {

      val handTags = taggingVotes.filter { _ match {
        case HandTagging(_, _, _) => true
        case _ => false
      }
      }.map(_.tag).distinct
      val indexTags = indexTagsService.indexTagsForTaggingVotes(taggingVotes)

      createFrontendResourceFrom(contentItem, loggedInUser, place, handTags, indexTags)
    }).flatten
  }

  def createFrontendResourceFrom(contentItem: Resource, loggedInUser: Option[User], place: Option[Geocode], handTags: Seq[Tag], indexTags: Seq[Tag])(implicit ec: ExecutionContext): Future[FrontendResource] = {
    mapFrontendResource(contentItem, place, handTags, indexTags, loggedInUser).map { frontendResource =>
      val actions = actionsFor(frontendResource, loggedInUser)
      frontendResource match {
        // TODO this match to call the same code on each class is a weird smell
        case n: FrontendNewsitem => n.copy(actions = actions)
        case f: FrontendFeed => f.copy(actions = actions)
        case l: FrontendWatchlist => l.copy(actions = actions)
        case w: FrontendWebsite => w.copy(actions = actions)
      }
    }
  }

  def mapFrontendResource(contentItem: Resource, place: Option[Geocode], handTags: Seq[Tag], indexTags: Seq[Tag], loggedInUser: Option[User])(implicit ec: ExecutionContext): Future[FrontendResource] = {
    val httpStatus = if (editPermissionService.canEdit(contentItem, loggedInUser)) {
      Some(contentItem.http_status)
    } else {
      None
    }

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
            name = n.title,
            url = n.page,
            date = n.date.orNull,
            description = n.description.orNull,
            geocode = place,
            acceptedFrom = feed,
            acceptedBy = acceptedByUser,
            accepted = n.accepted.orNull,
            image = null, // TODO
            urlWords = n.url_words.orNull,
            publisherName = publisher.map(_.title),
            publisherUrlWords = publisher.flatMap(_.url_words),
            httpStatus = httpStatus,
            lastScanned = n.last_scanned,
            lastChanged = n.last_changed,
            owner = owner.map(user => user.profilename.getOrElse(user._id.stringify)).orNull,
            tags = Some(indexTags),
            handTags = Some(handTags),
            twitterImage = n.twitterImage.orNull
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

        val acceptancePolicy = if (editPermissionService.canEdit(f, loggedInUser)) {
          Some(f.acceptance)
        } else {
          None
        }

        for {
          publisher <- eventualPublisher
          owner <- eventualOwner
        } yield {
          FrontendFeed(
            id = f.id,
            `type` = f.`type`,
            name = f.title,
            url = f.page,
            urlWords = f.url_words.orNull,
            date = f.date.orNull,
            description = f.description.orNull,
            geocode = place,
            latestItemDate = f.getLatestItemDate,
            lastRead = f.last_read,
            acceptancePolicy = acceptancePolicy,
            publisherName = publisher.map(_.title),
            publisherUrlWords = publisher.flatMap(_.url_words),
            httpStatus = httpStatus,
            lastScanned = f.last_scanned,
            lastChanged = f.last_changed,
            owner = owner.map(user => user.profilename.getOrElse(user._id.stringify)).orNull,
            tags = Some(indexTags),
            handTags = Some(handTags)
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
            name = l.title,
            url = l.page,
            date = l.date.orNull,
            publisherName = publisher.map(_.title),
            publisherUrlWords = publisher.flatMap(_.url_words),
            description = l.description.orNull,
            geocode = place,
            httpStatus = httpStatus,
            lastScanned = l.last_scanned,
            lastChanged = l.last_changed,
            owner = owner.map(user => user.profilename.getOrElse(user._id.stringify)).orNull,
            tags = Some(indexTags),
            handTags = Some(handTags)
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
            name = w.title,
            url = w.page,
            urlWords = w.url_words.orNull,
            description = w.description.getOrElse(""),
            geocode = w.geocode,
            httpStatus = httpStatus,
            date = w.date.orNull,
            lastScanned = w.last_scanned,
            lastChanged = w.last_changed,
            owner = owner.map(user => user.profilename.getOrElse(user._id.stringify)).orNull,
            tags = Some(indexTags),
            handTags = Some(handTags)
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
