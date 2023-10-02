package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.frontend.FrontendResource
import org.springframework.stereotype.Component
import org.springframework.ui.ModelMap

import scala.jdk.CollectionConverters._

@Component class CommonAttributesModelBuilder extends CommonSizes
  with ReasonableWaits {

  def setRss(mv: ModelMap, title: String, url: String): Unit = {
    mv.addAttribute("rss_title", title)
    mv.addAttribute("rss_url", url)
  }

  def secondaryFeeds(feeds: Seq[FrontendResource], heading: String = "Updated Feeds",
                     description: String = "Recently updated feeds from local organisations."): ModelMap = {
    val mv = new ModelMap().
      addAttribute("righthand_heading", heading).
      addAttribute("righthand_description", description)
    if (feeds.nonEmpty) {
      mv.addAttribute("righthand_content", feeds.asJava)
    }
    mv
  }

}
