package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.frontend.FrontendResource
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

@Component class CommonAttributesModelBuilder @Autowired()(contentRetrievalService: ContentRetrievalService) extends CommonSizes
with ReasonableWaits {

  def setRss(mv: ModelAndView, title: String, url: String) {
    mv.addObject("rss_title", title)
    mv.addObject("rss_url", url)
  }

  def withSecondaryFeeds(mv: ModelAndView, feeds: Seq[FrontendResource], heading: String = "Updated Feeds",
                         description: String = "Recently updated feeds from local organisations."): ModelAndView = {
    mv.addObject("righthand_heading", heading)
    mv.addObject("righthand_description", description)
    if (feeds.nonEmpty) {
      import scala.collection.JavaConverters._
      mv.addObject("righthand_content", feeds.asJava)
    }
    mv
  }

}
