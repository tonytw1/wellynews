package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model._
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.HandTaggingService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{Await, ExecutionContext, Future}

@Component class LoginResourceOwnershipService @Autowired()(mongoRepository: MongoRepository,
                                                            contentUpdateService: ContentUpdateService,
                                                            handTaggingService: HandTaggingService)
  extends ReasonableWaits {

  // Typically used when an anonymous session user later signs into their actual account.
  // Reassign the session submissions to their long running account so that they don't lose them.
  def reassignOwnership(previousOwner: User, newOwner: User)(implicit ec: ExecutionContext): Unit = {
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
          contentUpdateService.update(withNewOwner)
        }
        Future.sequence(updateWrites)
      }
    }

    Await.result(eventualOwnerChanges, TenSeconds)

    Await.result(handTaggingService.transferVotes(previousOwner, newOwner), TenSeconds)

    Await.result(mongoRepository.removeUser(previousOwner), TenSeconds)
  }

}
