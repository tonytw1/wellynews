package nz.co.searchwellington.model

trait Website extends Resource {
  def getFeeds: Set[Feed]
  def getWatchlist: Set[Watchlist]
  def getUrlWords: String
}
