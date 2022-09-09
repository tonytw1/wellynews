package nz.co.searchwellington.tagging

import nz.co.searchwellington.feeds.whakaoko.model.Category
import nz.co.searchwellington.model.{Resource, Tag}
import nz.co.searchwellington.repositories.TagDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.StringTokenizer
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

@Component
class TagHintAutoTagger @Autowired()(tagDAO: TagDAO) {

  def suggestTags(resource: Resource)(implicit ec: ExecutionContext): Future[Set[Tag]] = {
    val resourceContent = resource.title + " " + resource.description
    val tokens = tokenise(resourceContent.toLowerCase())

    def matches(tag: Tag): Boolean = {
      val hints = tag.hints
      hints.exists { hint =>
        val hintTokens = tokenise(hint.toLowerCase)
        tokens.containsSlice(hintTokens)
      }
    }

    tagDAO.getAllTags.map { allTags =>
      allTags.filter(tag => matches(tag)).toSet
    }
  }

  def suggestFeedCategoryTags(feedItemCategories: Seq[Category])(implicit ec: ExecutionContext): Future[Set[Tag]] = {
    if (feedItemCategories.nonEmpty) {
      // Given all tags look for autohints with vaguely match the categories presented
      tagDAO.getAllTags.map { allTags =>
        allTags.filter { tag =>
          val intersections = tag.hints.map(_.toLowerCase).toSet.
            intersect(feedItemCategories.map(_.value.toLowerCase()).toSet)
          intersections.nonEmpty
        }.toSet
      }
    } else {
      Future.successful(Set.empty)
    }
  }

  private def tokenise(resourceContent: String): Seq[String] = {
    val standardTokenizer = new StringTokenizer(resourceContent)
    val tokens: mutable.ListBuffer[String] = ListBuffer.empty
    val iterator = standardTokenizer.asIterator()
    while (iterator.hasNext) {
      val value = iterator.next().asInstanceOf[String]
      tokens += value
    }
    tokens.toSeq
  }

}
