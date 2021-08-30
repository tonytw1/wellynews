package nz.co.searchwellington.modification

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.repositories.{HandTaggingService, TagDAO}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.Await

@Component class TagModificationService @Autowired()(var tagDAO: TagDAO, var handTaggingService: HandTaggingService) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[TagModificationService])

  def deleteTag(tag: Tag) {
    log.info("Deleting tag " + tag.getName)
    Await.result(handTaggingService.clearTaggingsForTag(tag), TenSeconds)
    if (tag.getParent != null) {
      //tag.getParent.getChildren.remove(tag)
    }
    tagDAO.deleteTag(tag)
  }

}
