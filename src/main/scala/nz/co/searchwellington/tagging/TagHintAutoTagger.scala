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

    def matches(resource: Resource, tag: Tag) : Boolean = {

      def resourceMatchesHint(resource: Resource, hint: String) : Boolean = {

        def matches(hint: String, value: String): Boolean = {
          !Strings.isNullOrEmpty(value) && value.toLowerCase().contains(hint.toLowerCase())
        }

        val headlineMatchesHint = resource.title.exists(t => matches(hint, t))
        val bodyMatchesTag = resource.description.exists(d => matches(hint, d))
        headlineMatchesHint || bodyMatchesTag
      }

      tag.getAutotagHints.exists { hintsString =>
        if (!Strings.isNullOrEmpty(hintsString)) {
          var hints = commaSplitter.split(hintsString)
          hints.exists(hint => resourceMatchesHint(resource, hint))
        } else {
          false
        }
      }
    }

    tagDAO.getAllTags().filter(tag => matches(resource, tag)).toSet
  }

}
