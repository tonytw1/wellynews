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

}
