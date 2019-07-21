package nz.co.searchwellington.linkchecking

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class LinkCheckerQueue  @Autowired() (linkChecker: LinkChecker) {

  def add(resourceId: String) = {
    linkChecker.scanResource(resourceId)
  }

}
