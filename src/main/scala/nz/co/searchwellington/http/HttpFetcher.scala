package nz.co.searchwellington.http

import java.net.URL

import scala.concurrent.{ExecutionContext, Future}

trait HttpFetcher {

  def httpFetch(url: URL)(implicit ec: ExecutionContext): Future[HttpFetchResult]

  def getUserAgent: String

}
