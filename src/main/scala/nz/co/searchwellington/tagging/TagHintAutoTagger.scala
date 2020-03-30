package nz.co.searchwellington.tagging

import com.google.common.base.{Splitter, Strings}
import nz.co.searchwellington.model.{Resource, Tag}
import nz.co.searchwellington.repositories.TagDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}

@Component
class TagHintAutoTagger @Autowired() (tagDAO: TagDAO) {

  private val commaSplitter: Splitter = Splitter.on(",")

  def suggestTags(resource: Resource)(implicit ec: ExecutionContext): Future[Set[Tag]] = {

    val resourceContent = (resource.title + " " + resource.description).toLowerCase

    def matches(resource: Resource, tag: Tag) : Boolean = {
      val autotagHints = tag.autotag_hints.map { autotagHints =>
        commaSplitter.split(autotagHints).map(_.trim).toSeq
      }.getOrElse(Seq.empty)

      val keywordsForTags = (autotagHints:+ tag.name.trim).filter(!Strings.isNullOrEmpty(_))

      keywordsForTags.exists(keyword => resourceContent.contains(keyword.toLowerCase))
    }

    tagDAO.getAllTags.map { allTags =>
      allTags.filter(tag => matches(resource, tag)).toSet
    }
  }

}
