package nz.co.searchwellington.linkchecking

import org.jsoup.Jsoup
import org.springframework.stereotype.Component
import org.springframework.util.DigestUtils
import uk.co.eelpieconsulting.common.html.HtmlCleaner

@Component
class PageContentHasher {

  def hashPageContent(pageContent: String): String = {
    val pageText = Jsoup.parse(pageContent).text()
    DigestUtils.md5DigestAsHex(pageText.getBytes())
  }

}
