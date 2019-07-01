package nz.co.searchwellington.linkchecking

import nz.co.searchwellington.model.Resource
import org.joda.time.DateTime

trait LinkCheckerProcessor {
  def process(checkResource: Resource, pageContent: String, seen: DateTime): Unit
}