package nz.co.searchwellington.tagging

import com.google.common.base.{Splitter, Strings}
import nz.co.searchwellington.model.{Resource, Tag}
import nz.co.searchwellington.repositories.TagDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._

@Component
class TagHintAutoTagger @Autowired() (tagDAO: TagDAO) {

  private val commaSplitter: Splitter = Splitter.on(",")

  def suggestTags(resource: Resource) : Set[Tag] = {
    tagDAO.getAllTags().filter(tag => matches(resource, tag)).toSet
  }

  private def matches(resource: Resource, tag: Tag) : Boolean = {
    if (!Strings.isNullOrEmpty(tag.getAutotagHints)) {
      var hints = commaSplitter.split(tag.getAutotagHints()).toList
      hints.exists(hint => resourceMatchesHint(resource, hint))
    } else {
      false;
    }
  }

  private def resourceMatchesHint(resource: Resource, hint: String) : Boolean = {
    val headlineMatchesHint = matches(hint, resource.getName())
    val bodyMatchesTag = matches(hint, resource.getDescription())
    headlineMatchesHint || bodyMatchesTag;
  }

  private def matches(hint: String, value: String): Boolean = {
    !Strings.isNullOrEmpty(value) && value.toLowerCase().contains(hint.toLowerCase())
  }

}