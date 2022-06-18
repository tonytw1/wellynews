package nz.co.searchwellington.urls

import java.net.MalformedURLException
import java.net.URL
import org.springframework.stereotype.Component

@Component
class UrlParser {
  def extractHostnameFrom(fullURL: String): String = try {
    new URL(fullURL).getHost
  } catch {
    case _: MalformedURLException =>
      null
  }
}
