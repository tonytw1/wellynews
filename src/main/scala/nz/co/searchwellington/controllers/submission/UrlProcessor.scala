package nz.co.searchwellington.controllers.submission

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.utils.UrlCleaner
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class UrlProcessor @Autowired()(urlCleaner: UrlCleaner) extends SubmissionProcessor {

  private val log = Logger.getLogger(classOf[UrlProcessor])
  private val URL = "url"

  override def process(request: HttpServletRequest, editResource: Resource): Unit = {
    val requestedUrl = request.getParameter(URL)
    if (requestedUrl != null) {
      val cleanedUrl = urlCleaner.cleanSubmittedItemUrl(requestedUrl)
      editResource.setUrl(cleanedUrl)
      log.debug("Resource url set to: " + cleanedUrl)
    }
  }

}
