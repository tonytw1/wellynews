package nz.co.searchwellington.linkchecking

import java.net.{URI, URL}

trait UrlWrangling {

  def isFullQualifiedUrl(uri: URI): Boolean = {
    uri.getScheme != null && uri.getAuthority != null
  }

  def expandUrlRelativeFrom(toExpand: URI, pageUrl: URL): URI = {
    pageUrl.toURI.resolve(toExpand)
  }
}
