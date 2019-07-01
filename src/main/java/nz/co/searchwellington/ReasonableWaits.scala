package nz.co.searchwellington

import scala.concurrent.duration.{Duration, MILLISECONDS, MINUTES, SECONDS}

trait ReasonableWaits {

  val ThirtySeconds = Duration(30, SECONDS)
  val TenSeconds = Duration(10, SECONDS)
  val OneMinute = Duration(1, MINUTES)
  val OneHundredMilliSeconds = Duration(100, MILLISECONDS)

}
