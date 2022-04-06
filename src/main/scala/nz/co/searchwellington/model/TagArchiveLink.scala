package nz.co.searchwellington.model

import org.joda.time.Interval

case class TagArchiveLink(tag: Tag, interval: Interval, count: Long) extends IntervalLink {
  def getTag: Tag = tag
}
