package nz.co.searchwellington.http

import scala.concurrent.{ExecutionContext, Future}

trait HttpFetcher {

  def httpFetch(url: String)(implicit ec: ExecutionContext): Future[HttpFetchResult]

  def getUserAgent: String

}
