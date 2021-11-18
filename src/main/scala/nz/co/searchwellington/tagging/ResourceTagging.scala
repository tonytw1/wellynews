package nz.co.searchwellington.tagging

import nz.co.searchwellington.model.{Tag, Tagged}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.{ExecutionContext, Future}

trait ResourceTagging {

  val mongoRepository: MongoRepository

  def getDistinctHandTagsForResource(resource: Tagged)(implicit ec: ExecutionContext): Future[Seq[Tag]] = {
    val distinctTagIds = resource.resource_tags.map(_.tag_id).distinct
    Future.sequence(distinctTagIds.map(mongoRepository.getTagByObjectId)).map { f =>
      f.flatten
    }
  }

}
