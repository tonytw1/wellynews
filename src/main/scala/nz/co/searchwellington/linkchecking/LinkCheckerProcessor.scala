package nz.co.searchwellington.linkchecking

import nz.co.searchwellington.model.Resource
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext

trait LinkCheckerProcessor {
  def process(checkResource: Resource, pageContent: String, seen: DateTime)(implicit ec: ExecutionContext): Unit
}