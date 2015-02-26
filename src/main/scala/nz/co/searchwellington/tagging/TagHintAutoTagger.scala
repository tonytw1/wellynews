package nz.co.searchwellington.tagging

import com.google.common.base.{Splitter, Strings}
import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.repositories.TagDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scala.collection.JavaConversions._

@Component
class TagHintAutoTagger @Autowired() (tagDAO: TagDAO) {

  private val commaSplitter: Splitter = Splitter.on(",")

  def suggestTags(resource: Resource) : java.util.Set[Tag] = {
    def suggestedTags = for {
        tag <- tagDAO.getAllTags()
        if matches(resource, tag)
      } yield tag;

    val toSet = suggestedTags.toSet
    return toSet
  }

  private def matches(resource: Resource, tag: Tag) : Boolean = {
    if (!Strings.isNullOrEmpty(tag.getAutotagHints)) {
      var hints: List[String] = commaSplitter.split(tag.getAutotagHints()).toList
      return hints.exists(hint => resourceMatchesHint(resource, hint))
    }
    return false;
  }

  private def resourceMatchesHint(resource: Resource, hint: String) : Boolean = {
    val headlineMatchesHint = resource.getName().toLowerCase().contains(hint.toLowerCase())
    val bodyMatchesTag = resource.getDescription().toLowerCase().contains(hint.toLowerCase())
    return headlineMatchesHint || bodyMatchesTag;
  }

}