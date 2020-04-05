package nz.co.searchwellington.model

import java.util.Date

import nz.co.searchwellington.model.frontend.FrontendResource
import org.joda.time.Interval

case class PublisherArchiveLink(var publisher: FrontendResource, var interval: Interval, var count: Long) {

  def getPublisher: FrontendResource = publisher
  def getCount: Long = count
  def getMonth: Date = interval.getStart.toDate

}