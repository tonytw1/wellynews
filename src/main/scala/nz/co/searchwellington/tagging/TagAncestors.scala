package nz.co.searchwellington.tagging

import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.repositories.mongo.MongoRepository

import scala.concurrent.{ExecutionContext, Future}

trait TagAncestors {

  def mongoRepository: MongoRepository

  def parentsOf(tag: Tag, soFar: Seq[Tag] = Seq.empty)(implicit ec: ExecutionContext): Future[Seq[Tag]] = {
    tag.parent.map { pid =>
      mongoRepository.getTagByObjectId(pid).flatMap { pto =>
        pto.map { p =>
          parentsOf(p, soFar :+ p)
        }.getOrElse {
          Future.successful(soFar)
        }
      }
    }.getOrElse {
      Future.successful(soFar)
    }
  }

  def descendantsOf(tag: Tag)(implicit ec: ExecutionContext): Future[Seq[Tag]] = {
    val eventualChildTags = mongoRepository.getTagsByParent(tag._id)
    eventualChildTags.flatMap { childTags =>
      val eventualChildrensDescendants = childTags.map(descendantsOf)
      Future.sequence(eventualChildrensDescendants).map { childrensDescendants =>
        childTags ++ childrensDescendants.flatten
      }
    }
  }

}
