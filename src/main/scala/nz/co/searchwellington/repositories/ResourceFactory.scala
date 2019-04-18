package nz.co.searchwellington.repositories

import nz.co.searchwellington.model._
import org.springframework.stereotype.Component

@Component class ResourceFactory {

  def createNewDiscoveredFeed(discoveredUrl: String): DiscoveredFeed = DiscoveredFeed(url = discoveredUrl)

}
