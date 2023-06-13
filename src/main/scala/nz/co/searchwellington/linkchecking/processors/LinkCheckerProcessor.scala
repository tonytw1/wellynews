package nz.co.searchwellington.linkchecking.processors

import nz.co.searchwellington.model.Resource
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

trait LinkCheckerProcessor {
  def process(checkResource: Resource, pageContent: Option[String], seen: DateTime)(implicit ec: ExecutionContext): Future[Resource]
}