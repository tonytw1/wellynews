package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

@Component class CommonAttributesModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService) extends CommonSizes {

  def setRss(mv: ModelAndView, title: String, url: String) {
    mv.addObject("rss_title", title)
    mv.addObject("rss_url", url)
  }

  final def populateSecondaryFeeds(mv: ModelAndView) {
    mv.addObject("righthand_heading", "Local Feeds")
    mv.addObject("righthand_description", "Recently updated feeds from local organisations.")
    val allFeeds = contentRetrievalService.getAllFeedsOrderByLatestItemDate()
    if (allFeeds.nonEmpty) {
      import scala.collection.JavaConverters._
      mv.addObject("righthand_content", allFeeds.asJava)
    }
  }

}
