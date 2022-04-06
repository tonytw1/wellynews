package nz.co.searchwellington.model

import java.util.Date

import org.joda.time.Interval

trait IntervalLink {
  def interval: Interval
  def count: Long
  def getCount: Long = count
  def getMonth: Date = interval.getStart.toDate
}

case class ArchiveLink(interval: Interval, count: Long) extends IntervalLink

