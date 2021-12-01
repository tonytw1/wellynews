package nz.co.searchwellington.tagging

import nz.co.searchwellington.feeds.whakaoko.model.Category
import nz.co.searchwellington.model.{Resource, Tag}
import nz.co.searchwellington.repositories.TagDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

@Component
class TagHintAutoTagger @Autowired()(tagDAO: TagDAO) {

  def suggestTags(resource: Resource)(implicit ec: ExecutionContext): Future[Set[Tag]] = {

    def matches(resourceContent: String, tag: Tag): Boolean = {
      tag.autoTagHints.exists(keyword => resourceContent.contains(keyword.toLowerCase))
    }

    val resourceContent = (resource.title + " " + resource.description).toLowerCase
    tagDAO.getAllTags.map { allTags =>
      allTags.filter(tag => matches(resourceContent, tag)).toSet
    }
  }

  def suggestFeedCategoryTags(feedItemCategories: Seq[Category])(implicit ec: ExecutionContext): Future[Set[Tag]] = {
    if (feedItemCategories.nonEmpty) {
      // Given all tags look for autohints with vaguely match the categories presented
      tagDAO.getAllTags.map { allTags =>
        allTags.filter { tag =>
          val intersections = tag.autotag_hints.toSet.intersect(feedItemCategories.map(_.value).toSet)
          intersections.nonEmpty
        }.toSet
      }
    } else {
      Future.successful(Set.empty)
    }
  }

}
