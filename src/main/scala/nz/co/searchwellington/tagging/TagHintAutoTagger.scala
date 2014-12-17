package nz.co.searchwellington.tagging

import com.google.common.base.{Splitter, Strings}
import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.repositories.TagDAO
import scala.collection.JavaConversions._

class TagHintAutoTagger(tagDAO: TagDAO) {

  val commaSplitter: Splitter = Splitter.on(",")

  def suggestTags(resource: Resource) : java.util.Set[Tag] = {
    def suggestedTags = for {
        tag <- tagDAO.getAllTags()
        if matches(resource, tag)
      } yield tag;
    return suggestedTags.toSet
  }

  def matches(resource: Resource, tag: Tag) : Boolean = {
    if (!Strings.isNullOrEmpty(tag.getAutotagHints)) {
      commaSplitter.split(tag.getAutotagHints()).toList.foreach(hint =>
        if (checkForMatch(resource, hint)) {
          return true;
        }
      )
    }
    return false;
  }

  def checkForMatch(resource: Resource, hint: String) : Boolean = {
    val headlineMatchesHint = resource.getName().toLowerCase().contains(hint.toLowerCase())
    val bodyMatchesTag = resource.getDescription().toLowerCase().contains(hint.toLowerCase())
    return headlineMatchesHint || bodyMatchesTag;
  }

}