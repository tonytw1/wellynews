package nz.co.searchwellington.linkchecking

import org.springframework.stereotype.Component
import org.springframework.util.DigestUtils
import uk.co.eelpieconsulting.common.html.HtmlCleaner

@Component
class PageContentHasher {

  private val htmlCleaner = new HtmlCleaner

  def hashPageContent(pageContent: String): String = {
    // TODO cleaning and filtering?
    DigestUtils.md5DigestAsHex(htmlCleaner.stripHtml(pageContent).getBytes())
    }

}
