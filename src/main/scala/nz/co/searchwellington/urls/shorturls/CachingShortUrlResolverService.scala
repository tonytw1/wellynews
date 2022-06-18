package nz.co.searchwellington.urls.shorturls

import io.micrometer.core.instrument.MeterRegistry
import org.apache.commons.codec.digest.DigestUtils
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.caching.MemcachedCache
import uk.co.eelpieconsulting.common.shorturls.resolvers._

import java.net.URL

@Component
class CachingShortUrlResolverService @Autowired()(cache: MemcachedCache, registry: MeterRegistry) {

  private val log = Logger.getLogger(classOf[CachingShortUrlResolverService])
  private val ONE_DAY = 3600 * 24
  private val KEY_PREFIX = "resolved-shorturls-urls::"

  private val shortUrlResolverService = new CompositeUrlResolver(new BitlyUrlResolver, new FeedBurnerRedirectResolver, new TinyUrlResolver, new TwitterShortenerUrlResolver)

  private val callsCounter = registry.counter("shorturlresolver_called")
  private val cachedCounter = registry.counter("shorturlresolver_cached")
  private val resolvedCounter = registry.counter("shorturlresolver_resolved")
  private val errorsCounter = registry.counter("shorturlresolver_errors")

  def resolveUrl(url: URL): URL = {
    callsCounter.increment()
    try {
      if (!shortUrlResolverService.isValid(url)) {
        return url
      }

      val cachedResult = cache.get(generateKey(url)).asInstanceOf[URL]
      if (cachedResult != null) {
        log.debug("Found result for url '" + url.toExternalForm + "' in cache: " + cachedResult)
        cachedCounter.increment()
        return cachedResult
      }
      log.debug("Delegating to live url resolver")
      resolvedCounter.increment()
      val resolved = shortUrlResolverService.resolveUrl(url)
      if (resolved != null) {
        putUrlIntoCache(resolved, resolved)
        return resolved
      }
      log.warn("Short url resolver returned null for: " + url.toExternalForm + "; returning input url")
      url

    } catch {
      case e: Exception =>
        log.error("Failed to resolve short url for '" + url.toExternalForm + "': ", e)
        errorsCounter.increment()
        throw e
    }
  }

  private def putUrlIntoCache(url: URL, resolved: URL): Unit = {
    log.debug("Caching resolved result for url: " + url.toExternalForm)
    cache.put(generateKey(url), ONE_DAY, resolved)
  }

  private def generateKey(url: URL): String = {
    KEY_PREFIX + DigestUtils.sha256Hex(url.toExternalForm)
  }

}