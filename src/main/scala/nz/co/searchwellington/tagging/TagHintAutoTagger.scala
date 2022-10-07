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
    val resourceTokens = tokenise(resourceContent.toLowerCase())

    tagDAO.getAllTags.map { allTags =>
      allTags.filter(tag => matches(tag.hints, resourceTokens)).toSet
    }
  }

  def suggestFeedCategoryTags(feedItemCategories: Seq[Category])(implicit ec: ExecutionContext): Future[Set[Tag]] = {
    if (feedItemCategories.nonEmpty) {
      // Given all tags look for autohints with vaguely match the categories presented
      val categoryTokens = feedItemCategories.map(_.value.toLowerCase()).map(tokenise)

      tagDAO.getAllTags.map { allTags =>
        allTags.filter { tag =>
          categoryTokens.exists { categoryTokens =>
            matches(tag.hints, categoryTokens)
          }
        }.toSet
      }
    } else {
      Future.successful(Set.empty)
    }
  }

  private def matches(hints: Seq[String], resourceTokens: Seq[String]): Boolean = {
    hints.exists { hint =>
      val hintTokens = tokenise(hint.toLowerCase)
      resourceTokens.containsSlice(hintTokens)
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
