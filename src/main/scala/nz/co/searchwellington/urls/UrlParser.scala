package nz.co.searchwellington.urls

import java.net.MalformedURLException
import java.net.URL
import org.springframework.stereotype.Component

import scala.util.Try

@Component
class UrlParser {

  def extractHostnameFrom(fullURL: String): Option[String] = Try {
    new URL(fullURL)
  }.toOption.flatMap { url =>
    Option(url.getHost)
  }
  
}
