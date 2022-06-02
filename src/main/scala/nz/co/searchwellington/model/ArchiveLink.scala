package nz.co.searchwellington.model

import org.joda.time.Interval

import java.time.LocalDate
import java.util.Date

trait IntervalLink {
  def interval: Interval
  def count: Option[Long]
  def getCount: Long = count.getOrElse(0)
  def getMonth: Date = interval.getStart.toDate
}

case class ArchiveLink(interval: Interval, count: Option[Long]) extends IntervalLink

case class AcceptedDay(day: LocalDate, count: Long) {
  def getDay: LocalDate = day
  def getCount: Long = count
}
