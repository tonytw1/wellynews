package nz.co.searchwellington.tagging

import nz.co.searchwellington.model.{Tag, Tagged}
import nz.co.searchwellington.repositories.HandTaggingDAO

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ResourceTagging {

  val handTaggingDAO: HandTaggingDAO

  def getHandTagsForResource(resource: Tagged): Future[Seq[Tag]] = {
    handTaggingDAO.getHandTaggingsForResource(resource).map { handTaggings => // TODO just needs to load tags; not users
      handTaggings.map(_.tag).distinct
    }
  }

}
