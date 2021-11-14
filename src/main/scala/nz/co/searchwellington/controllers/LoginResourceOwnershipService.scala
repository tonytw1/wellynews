package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Feed, Newsitem, Resource, User, Watchlist, Website}
import nz.co.searchwellington.repositories.HandTaggingService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.api.commands.WriteResult

import scala.concurrent.{Await, ExecutionContext, Future}

@Component class LoginResourceOwnershipService @Autowired()(mongoRepository: MongoRepository, handTaggingService: HandTaggingService)
  extends ReasonableWaits {

  // Typically used when an anonymous session user later signs into their actual account.
  // Reassign the session submissions to their long running account so that they don't lose them.
  def reassignOwnership(previousOwner: User, newOwner: User)(implicit ec: ExecutionContext) {
    val eventualOwnerChanges = mongoRepository.getResourcesIdsOwnedBy(previousOwner).flatMap { rids =>
      val eventualMaybeResourcesIds: Seq[Future[Option[Resource]]] = rids.map { id =>
        mongoRepository.getResourceByObjectId(id)
      }

      Future.sequence(eventualMaybeResourcesIds).flatMap { ros: Seq[Option[Resource]] =>
        val updateWrites = ros.flatten.map { resource =>
          val withNewOwner = resource match {
            case w: Website => w.copy(owner = Some(newOwner._id))
            case n: Newsitem => n.copy(owner = Some(newOwner._id))
            case f: Feed => f.copy(owner = Some(newOwner._id))
            case l: Watchlist => l.copy(owner = Some(newOwner._id))
          }
          mongoRepository.saveResource(withNewOwner)  // TODO need an elastic update as well.
        }
        Future.sequence(updateWrites)
      }
    }

    Await.result(eventualOwnerChanges, TenSeconds)

    Await.result(handTaggingService.transferVotes(previousOwner, newOwner), TenSeconds)

    Await.result(mongoRepository.removeUser(previousOwner), TenSeconds)
  }

}
