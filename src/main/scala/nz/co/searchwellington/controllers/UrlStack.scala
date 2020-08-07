package nz.co.searchwellington.controllers

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class UrlStack @Autowired()(urlBuilder: UrlBuilder) {

  private val log = Logger.getLogger(classOf[UrlStack])

  def getExitUrlFromStack(request: HttpServletRequest): String = {
    val stackUrl = request.getSession.getAttribute("url").asInstanceOf[String]
    if (stackUrl != null) {
      log.debug("Stack url is: " + stackUrl)
      urlBuilder.fullyQualified(stackUrl)
    } else {
      urlBuilder.getHomeUrl
    }
  }

  def setUrlStack(request: HttpServletRequest) {
    var url = RequestPath.getPathFrom(request)
    if (request.getQueryString != null) {
      url = url + "?" + request.getQueryString
    }
    setUrlStack(request, url)
  }

  def setUrlStack(request: HttpServletRequest, url: String) {
    request.getSession.setAttribute("url", url)
    log.debug("Put url onto the stack: " + url)
  }

}