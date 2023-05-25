package nz.co.searchwellington.urls

import java.io.UnsupportedEncodingException
import java.net.URLEncoder

@Deprecated
// Suggests that we the URLs builders are not using a higher level URL library if they need help with encoding parameters
object UrlParameterEncoder {

  private val UTF_8 = "UTF-8"

  def encode(value: String): String = try URLEncoder.encode(value, UTF_8)
  catch {
    case e: UnsupportedEncodingException =>
      throw new RuntimeException(e)
  }

}