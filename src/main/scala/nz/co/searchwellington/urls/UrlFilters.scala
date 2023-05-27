package nz.co.searchwellington.urls

import com.google.common.base.Strings
import org.apache.http.NameValuePair
import org.apache.http.client.utils.URIBuilder

import java.net.URL
import java.util
import java.util.regex.Pattern

object UrlFilters {
  private val HTTP_PREFIX = "http://"
  private val PHPSESSION_PARAMETER = Pattern.compile("^" + "PHPSESSID" + "$")
  private val UTM_PARAMETERS = Pattern.compile("^utm_.*$")

  def addHttpPrefixIfMissing(url: String): String = {
    if (!Strings.isNullOrEmpty(url) && !hasHttpPrefix(url)) addHttpPrefix(url) else url
  }

  def hasHttpPrefix(url: String): Boolean = url.startsWith("http://") || url.startsWith("https://")

  private def addHttpPrefix(url: String): String = HTTP_PREFIX + url

  def stripPhpSession(url: URL): URL = removeQueryParametersFrom(url, PHPSESSION_PARAMETER)

  def stripUTMParams(url: URL): URL = removeQueryParametersFrom(url, UTM_PARAMETERS)

  private def removeQueryParametersFrom(url: URL, p: Pattern) = {
    val uriBuilder = new URIBuilder(url.toURI)
    import scala.jdk.CollectionConverters._
    val params = uriBuilder.getQueryParams.asScala.toSeq
    val filteredParams = new util.ArrayList[NameValuePair]
    params.foreach { param: NameValuePair =>
      if (!p.matcher(param.getName).matches) {
        filteredParams.add(param)
      }
    }
    uriBuilder.setParameters(filteredParams).build.toURL
  }

}