package nz.co.searchwellington.modification

import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.repositories.{HandTaggingService, TagDAO}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class TagModificationService @Autowired()(var tagDAO: TagDAO, var handTaggingService: HandTaggingService) {

  private val log = Logger.getLogger(classOf[TagModificationService])

  def updateTagParent(editTag: Tag, parentTag: Tag) {
    log.debug("Setting parent tag to: " + parentTag.getName)
    editTag.setParent(parentTag)
  }

  def deleteTag(tag: Tag) {
    log.info("Deleting tag " + tag.getName)
    handTaggingService.clearTaggingsForTag(tag)
    if (tag.getParent != null) {
      tag.getParent.getChildren.remove(tag)
    }
    tagDAO.deleteTag(tag)
  }

}