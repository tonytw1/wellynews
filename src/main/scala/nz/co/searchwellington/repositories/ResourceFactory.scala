package nz.co.searchwellington.repositories

import java.util

import com.google.common.collect.Lists
import nz.co.searchwellington.model._
import org.springframework.stereotype.Component

@Component class ResourceFactory {

  def createNewDiscoveredFeed(discoveredUrl: String): DiscoveredFeed = {
    val discoveredFeed = new DiscoveredFeed
    discoveredFeed.setUrl(discoveredUrl)
    discoveredFeed.setReferences(new util.HashSet[Resource])
    discoveredFeed
  }

}
