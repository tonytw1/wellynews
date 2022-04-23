package nz.co.searchwellington.model

import java.util.Date

import org.joda.time.Interval

trait IntervalLink {
  def interval: Interval
  def count: Option[Long]
  def getCount: java.lang.Long = count.getOrElse(0L)
  def getMonth: Date = interval.getStart.toDate
}

case class ArchiveLink(interval: Interval, count: Option[Long]) extends IntervalLink
