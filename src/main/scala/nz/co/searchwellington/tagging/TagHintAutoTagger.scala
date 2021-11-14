package nz.co.searchwellington.tagging

import com.google.common.base.{Splitter, Strings}
import nz.co.searchwellington.model.{Resource, Tag}
import nz.co.searchwellington.repositories.TagDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

@Component
class TagHintAutoTagger @Autowired()(tagDAO: TagDAO) {

  private val commaSplitter = Splitter.on(",")

  def suggestTags(resource: Resource)(implicit ec: ExecutionContext): Future[Set[Tag]] = {

    def matches(resourceContent: String, tag: Tag): Boolean = {
      val autotagHints = tag.autotag_hints.map { autotagHints =>
        commaSplitter.split(autotagHints).asScala.map(_.trim).toSeq
      }.getOrElse(Seq.empty)

      val keywordsForTags = autotagHints.filter(!Strings.isNullOrEmpty(_))

      keywordsForTags.exists(keyword => resourceContent.contains(keyword.toLowerCase))
    }

    val resourceContent = (resource.title + " " + resource.description).toLowerCase
    tagDAO.getAllTags.map { allTags =>
      allTags.filter(tag => matches(resourceContent, tag)).toSet
    }
  }

}
