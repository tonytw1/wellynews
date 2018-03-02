package nz.co.searchwellington.spam

import org.springframework.stereotype.Component
import nz.co.searchwellington.model.Resource

@Component class SpamFilter {
  def isSpam(editResource: Resource): Boolean = {
    val urlIsSpam = editResource.page.map(p => p.contains("rfid")).getOrElse(false)
    val descriptionsSpam = editResource.description.map(d => d.contains("rfid")).getOrElse(false)
    urlIsSpam || descriptionsSpam
  }
}
