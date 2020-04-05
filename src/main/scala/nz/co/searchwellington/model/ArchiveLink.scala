package nz.co.searchwellington.model

import java.util.Date

import org.joda.time.Interval

case class ArchiveLink(var interval: Interval, var count: Long) {

  def getCount: Long = count
  def getMonth: Date = interval.getStart.toDate

}
