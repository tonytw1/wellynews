package nz.co.searchwellington.spam

import org.springframework.stereotype.Component
import nz.co.searchwellington.model.Resource

@Component
class SpamFilter {
  def isSpam(editResource: Resource): Boolean = {
    val urlIsSpam = editResource.page.contains("rfid")
    val descriptionsSpam = editResource.description.exists(d => d.contains("rfid"))
    urlIsSpam || descriptionsSpam
  }
}
