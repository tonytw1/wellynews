package nz.co.searchwellington.urls

import nz.co.searchwellington.urls.shorturls.CachingShortUrlResolverService
import nz.co.searchwellington.utils.UrlFilters
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL

@Component
class UrlCleaner @Autowired()(shortUrlResolver: CachingShortUrlResolverService) {
  final private val log = LogFactory.getLog(classOf[UrlCleaner])

  // Given a user or feed supplied URL attempt to reduce it to a more canonical form by stripping out things like sessions ids and utm params
  def cleanSubmittedItemUrl(url: URL) = try {
    // Resolve short urls
    var expanded = shortUrlResolver.resolveUrl(url)

    // Strip obvious per request artifacts from the url to help with duplicate detection
    expanded = UrlFilters.stripUTMParams(expanded)
    expanded = UrlFilters.stripPhpSession(expanded)

    log.debug("Cleaned url is: " + expanded.toExternalForm)
    expanded

  } catch {
    case _: URISyntaxException | _: MalformedURLException =>
      log.warn("Invalid URL given; returning unaltered: " + url.toExternalForm)
      url
  }

}