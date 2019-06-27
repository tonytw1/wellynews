package nz.co.searchwellington.model

import java.util.Date

case class ArchiveLink(var month: Date, var count: Long) {

  def getCount: Long = count
  def getMonth: Date = month

}