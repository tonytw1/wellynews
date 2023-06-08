package nz.co.searchwellington.http

import java.net.URL

import scala.concurrent.{ExecutionContext, Future}

trait HttpFetcher {

  def httpFetch(url: URL, followRedirects: Boolean = true)(implicit ec: ExecutionContext): Future[HttpFetchResult]

  def getUserAgent: String

}
