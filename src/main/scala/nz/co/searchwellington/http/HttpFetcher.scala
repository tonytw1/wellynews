package nz.co.searchwellington.http

import nz.co.searchwellington.utils.HttpFetchResult

trait HttpFetcher {

  def httpFetch(url: String): HttpFetchResult

  def getUserAgent: String

}
