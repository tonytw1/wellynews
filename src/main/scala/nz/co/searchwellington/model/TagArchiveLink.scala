package nz.co.searchwellington.model

import org.joda.time.Interval

import java.util.Date

case class TagArchiveLink(tag: Tag, interval: Interval, count: Long) {

  def getTag: Tag = tag
  def getCount: Long = count
  def getMonth: Date = interval.getStart.toDate

}