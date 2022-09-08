package nz.co.searchwellington.linkchecking

import nz.co.searchwellington.utils.UrlFilters
import org.springframework.stereotype.Component
import org.springframework.util.DigestUtils

@Component
class PageContentHasher {

  def hashPageContent(pageContent: String): String = {
    // TODO cleaning and filtering?
    DigestUtils.md5DigestAsHex(UrlFilters.stripHtml(pageContent).getBytes())
    }

}
