package nz.co.searchwellington.model

import nz.co.searchwellington.model.frontend.FrontendResource
import org.joda.time.Interval

case class PublisherArchiveLink(publisher: FrontendResource, interval: Interval, count: Option[Long]) extends IntervalLink {

  def getPublisher: FrontendResource = publisher

}