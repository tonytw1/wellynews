package nz.co.searchwellington.tagging

import java.util.Iterator
import java.util.List
import java.util.Set

import com.google.common.base.{Splitter, Strings}
import com.google.common.collect.{Lists, Sets}
import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.repositories.TagDAO

class TagHintAutoTagger(tagDAO: TagDAO) {

  def suggestTags(resource: Resource) : Set[Tag] = {
    val suggestedTags: Set[Tag] = Sets.newHashSet();

    val it: Iterator[Tag] = tagDAO.getAllTags().iterator()
    while(it.hasNext()) {
      var tag: Tag = it.next()
      if (!Strings.isNullOrEmpty(tag.getAutotagHints())) {
        val hints = Lists.newArrayList(Splitter.on(",").split(tag.getAutotagHints()))
        suggestedTags.addAll(process(resource, tag, hints))
      }
    }
    return suggestedTags
  }

  def process(resource: Resource, tag: Tag, hints: List[String]) : Set[Tag] = {
    val suggestedTags: Set[Tag] = Sets.newHashSet();

    val it: Iterator[String] = hints.iterator();
    while(it.hasNext) {
      val hint = it.next
      if (checkForMatch(resource, hint)) {
        suggestedTags.add(tag);
      }
    }
    return suggestedTags;
  }

  def checkForMatch(resource: Resource, hint: String) : Boolean = {
    val headlineMatchesHint = resource.getName().toLowerCase().contains(hint.toLowerCase())
    val bodyMatchesTag = resource.getDescription().toLowerCase().contains(hint.toLowerCase())
    return headlineMatchesHint || bodyMatchesTag;
  }

}