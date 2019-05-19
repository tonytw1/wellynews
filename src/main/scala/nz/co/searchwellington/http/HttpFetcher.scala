package nz.co.searchwellington.http

trait HttpFetcher {

  def httpFetch(url: String): HttpFetchResult

  def getUserAgent: String

}
