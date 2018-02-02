package nz.co.searchwellington.model

import java.util

@SerialVersionUID(1L)
case class Watchlist() extends PublishedResourceImpl {
  def this(id: Int, name: String, url: String, description: String, publisher: Website, discoveredFeeds: util.Set[DiscoveredFeed]) {
    this()
    this.id = id
    this.name = name
    this.url = url
    this.description = description
    this.publisher = publisher
    this.discoveredFeeds = discoveredFeeds
  }

  override def getType = "L"
}
