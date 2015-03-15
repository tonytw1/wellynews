package nz.co.searchwellington.controllers

import java.util.Map
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.google.common.collect.Maps
import nz.co.searchwellington.model.SiteInformation
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory

@Controller class RssController @Autowired() (siteInformation: SiteInformation, contentRetrievalService: ContentRetrievalService, rssUrlBuilder: RssUrlBuilder, viewFactory: ViewFactory) {

  @RequestMapping(Array("/rss"))
  @throws(classOf[Exception])
  def mainRss(request: HttpServletRequest, response: HttpServletResponse): ModelAndView = {
    val title: String = siteInformation.getAreaname + " Newslog"
    val link: String = siteInformation.getUrl
    val description: String = "Links to " + siteInformation.getAreaname + " related newsitems."

    val model: Map[String, AnyRef] = Maps.newHashMap();
    model.put("data", contentRetrievalService.getLatestNewsitems)
    new ModelAndView(viewFactory.getRssView(title, link, description), model)
  }

}