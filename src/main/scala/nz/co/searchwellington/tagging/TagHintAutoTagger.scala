package nz.co.searchwellington.tagging

import com.google.common.base.{Splitter, Strings}
import com.google.common.collect.Sets
import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.repositories.TagDAO
import scala.collection.JavaConversions._

class TagHintAutoTagger(tagDAO: TagDAO) {

  val commaSplitter: Splitter = Splitter.on(",")

  def suggestTags(resource: Resource) : java.util.Set[Tag] = {
    val suggestedTags: java.util.Set[Tag] = Sets.newHashSet();
    for (
      tag <- tagDAO.getAllTags()
      if Strings.isNullOrEmpty(tag.getAutotagHints)) {
        suggestedTags.addAll(process(resource, tag, commaSplitter.split(tag.getAutotagHints()).toList));
    }
    return suggestedTags
  }

  def process(resource: Resource, tag: Tag, hints: List[String]) : java.util.Set[Tag] = {
    val suggestedTags: java.util.Set[Tag] = Sets.newHashSet();
    hints.foreach(hint =>
      if (checkForMatch(resource, hint)) {
        suggestedTags.add(tag);
      }
    )
    return suggestedTags;
  }

  def checkForMatch(resource: Resource, hint: String) : Boolean = {
    val headlineMatchesHint = resource.getName().toLowerCase().contains(hint.toLowerCase())
    val bodyMatchesTag = resource.getDescription().toLowerCase().contains(hint.toLowerCase())
    return headlineMatchesHint || bodyMatchesTag;
  }

}