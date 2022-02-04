package nz.co.searchwellington.linkchecking

import org.apache.commons.logging.LogFactory

import java.net.{MalformedURLException, URI, URL}

trait UrlWrangling {

  @Deprecated
  def isFullQualified(url: String): Boolean = url.startsWith("http://") || url.startsWith("https://")

  def isFullQualifiedUrl(uri: URI): Boolean = {
    uri.getScheme != null && uri.getAuthority != null;
  }

  def expandUrlRelativeFrom(url: String, pageUrl: URL): String = {
    pageUrl.toURI.resolve(new URI(url)).toString
  }
}
