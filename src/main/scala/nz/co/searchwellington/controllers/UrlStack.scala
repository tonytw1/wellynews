package nz.co.searchwellington.controllers

import jakarta.servlet.http.HttpServletRequest
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class UrlStack @Autowired()(urlBuilder: UrlBuilder) {

  private val log = LogFactory.getLog(classOf[UrlStack])

  def getExitUrlFromStack(request: HttpServletRequest): String = {
    Option(request.getSession.getAttribute("url").asInstanceOf[String]).map { uri =>
      log.debug("Stack url is: " + uri)
      urlBuilder.fullyQualified(uri)
    }.getOrElse{
      urlBuilder.fullyQualified(urlBuilder.getHomeUri)
    }
  }

  def setUrlStack(request: HttpServletRequest): Unit = {
    var url = RequestPath.getPathFrom(request)
    if (request.getQueryString != null) {
      url = url + "?" + request.getQueryString
    }
    setUrlStack(request, url)
  }

  def setUrlStack(request: HttpServletRequest, url: String): Unit = {
    request.getSession.setAttribute("url", url)
    log.debug("Put url onto the stack: " + url)
  }

}