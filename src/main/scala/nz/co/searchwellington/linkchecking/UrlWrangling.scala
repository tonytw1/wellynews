package nz.co.searchwellington.linkchecking

import org.apache.commons.logging.LogFactory

import java.net.{MalformedURLException, URL}

trait UrlWrangling {

  private val log = LogFactory.getLog(classOf[FeedAutodiscoveryProcesser])

  def isFullQualified(discoveredUrl: String): Boolean = discoveredUrl.startsWith("http://") || discoveredUrl.startsWith("https://")

  def expandUrlRelativeFrom(url: String, pageUrl: URL): String = {
    try {
      val sitePrefix = pageUrl.getProtocol + "://" + pageUrl.getHost
      val fullyQualifiedUrl = sitePrefix + url
      fullyQualifiedUrl

    } catch {
      case e: MalformedURLException =>
        log.error("Invalid url", e)
        url
      case e: Throwable =>
        log.error("Invalid url", e)
        url
    }
  }
}
