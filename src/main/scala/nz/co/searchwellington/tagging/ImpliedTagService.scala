package nz.co.searchwellington.tagging

import nz.co.searchwellington.model.{Newsitem, Resource, Tag}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ImpliedTagService @Autowired() (taggingReturnsOfficerService: TaggingReturnsOfficerService) {

  def alreadyHasTag(resource: Resource, tag: Tag): Boolean = {
    val isNewsitemWhosPublisherAlreadyHasThisTag = resource.getType == "N" && resource.asInstanceOf[Newsitem].getPublisher != null && taggingReturnsOfficerService.getHandTagsForResource(resource.asInstanceOf[Newsitem].getPublisher).contains(tag)
    isNewsitemWhosPublisherAlreadyHasThisTag || taggingReturnsOfficerService.getHandTagsForResource(resource).contains(tag)
  }
  
}