package nz.co.searchwellington.modification

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.repositories.{HandTaggingService, TagDAO}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global // TODO

@Component class TagModificationService @Autowired()(var tagDAO: TagDAO, var handTaggingService: HandTaggingService) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[TagModificationService])

  def deleteTag(tag: Tag): Boolean = {
    log.info("Deleting tag " + tag.getName)
    val eventualOutcome = handTaggingService.clearTaggingsForTag(tag).flatMap { tagClearanceOutcome =>
      if (tag.getParent != null) {
        //tag.getParent.getChildren.remove(tag)
      }
      tagDAO.deleteTag(tag)
    }
    Await.result(eventualOutcome, ThirtySeconds)
  }

}
