package nz.co.searchwellington.repositories

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.taggingvotes.HandTagging
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.{ExecutionContext, Future}

@Deprecated // "tags are attached to resource document now"
@Component class HandTaggingDAO @Autowired()(mongoRepository: MongoRepository) extends ReasonableWaits {

  def getHandTaggingsForResource(resource: Tagged)(implicit ec: ExecutionContext): Future[Seq[HandTagging]] = {
    def resolveTagAndUser(tagging: Tagging): Future[Option[HandTagging]] = {
      val eventualMaybeTag = mongoRepository.getTagByObjectId(tagging.tag_id)
      val eventualMaybeUser = mongoRepository.getUserByObjectId(tagging.user_id)
      for {
        maybeTag <- eventualMaybeTag
        maybeUser <- eventualMaybeUser
      } yield for {
        tag <- maybeTag
        user <- maybeUser
      } yield {
        HandTagging(taggingUser = user, tag = tag, reason = tagging.reason)
      }
    }

    Future.sequence {
      resource.resource_tags.map(resolveTagAndUser)
    }.map(_.flatten)
  }

}
