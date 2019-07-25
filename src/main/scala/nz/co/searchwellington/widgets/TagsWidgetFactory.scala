package nz.co.searchwellington.widgets

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Feed, Tag}
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.ecs.html.{Option, Select}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Component class TagsWidgetFactory @Autowired() (tagDAO: TagDAO, mongoRepository: MongoRepository) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[TagsWidgetFactory])

  def createMultipleTagSelect(selectedTags: Set[Tag]): String = {
    val tagSelect = new Select("tags")
    tagSelect.setID("tags")
    tagSelect.setMultiple(true)
    selectedTags.map { tag =>
      val option: Option = new Option(tag.getName)
      option.setFilterState(true)
      option.addElement(tag.getName)
      tagSelect.addElement(option)
    }
    tagSelect.toString
  }

  def createTagSelect(name: String, selectedTag: Tag, tagsToExclude: Set[Tag]): Select = {
    createTagSelect(name, selectedTag, tagsToExclude, "No Parent")
  }

  def createRelatedFeedSelect(name: String, relatedFeed: Feed): Select = {
    val relatedFeedSelect = new Select(name)
    val noFeedOption = new Option("0")
    noFeedOption.addElement("No related Feed")
    if (relatedFeed == null) {
      noFeedOption.setSelected(true)
    }
    relatedFeedSelect.addElement(noFeedOption)

    Await.result(mongoRepository.getAllFeeds, TenSeconds).map { feed =>
      val option = new Option(feed.id)
      option.setFilterState(true)
      option.addElement(feed.title.getOrElse(feed.id.toString))
      if (relatedFeed != null && relatedFeed == feed) {
        option.setSelected(true)
      }
      relatedFeedSelect.addElement(option)
    }
    relatedFeedSelect
  }

  private def createTagSelect(name: String, selectedTag: Tag, tagsToExclude: Set[Tag], noneSelectedText: String): Select = {
    val tagSelect = new Select(name)
    tagSelect.setMultiple(false)
    val noParentOption = new Option("0")
    noParentOption.addElement(noneSelectedText)
    if (selectedTag == null) {
      noParentOption.setSelected(true)
    }
    tagSelect.addElement(noParentOption)

    Await.result(tagDAO.getAllTags, TenSeconds).map { tag =>
      val tagIsNotExcluded = !tagsToExclude.contains(tag)
      if (tagIsNotExcluded) {
        val option = new Option(tag.getName)
        option.setFilterState(true)
        option.addElement(tag.getDisplayName.toLowerCase)
        if (selectedTag != null && selectedTag == tag) {
          option.setSelected(true)
        }
        tagSelect.addElement(option)
      }
    }
    tagSelect
  }
}
