package nz.co.searchwellington.tagging

import com.google.common.base.{Splitter, Strings}
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Resource, Tag}
import nz.co.searchwellington.repositories.TagDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._
import scala.concurrent.Await

@Component
class TagHintAutoTagger @Autowired() (tagDAO: TagDAO) extends ReasonableWaits {

  private val commaSplitter: Splitter = Splitter.on(",")

  def suggestTags(resource: Resource) : Set[Tag] = {

    def matches(resource: Resource, tag: Tag) : Boolean = {

      def resourceMatchesHint(resource: Resource, hint: String) : Boolean = {
        def matches(hint: String, value: String): Boolean = {
          !Strings.isNullOrEmpty(value) && value.toLowerCase().contains(hint.toLowerCase())
        }
        resource.title.exists(t => matches(hint, t)) || resource.description.exists(d => matches(hint, d))
      }

      tag.getAutotagHints.exists { hintsString =>
        val hints = commaSplitter.split(hintsString)
        hints.exists(hint => resourceMatchesHint(resource, hint))
      }
    }

    val allTags = Await.result(tagDAO.getAllTags, TenSeconds)
    val tagsWithAutotaggingHints = allTags.filter(t => t.autotag_hints.exists(!Strings.isNullOrEmpty(_))) // TODO include the tag name?

    tagsWithAutotaggingHints.filter(tag => matches(resource, tag)).toSet
  }

}
