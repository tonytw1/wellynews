package nz.co.searchwellington

import scala.concurrent.duration.{Duration, FiniteDuration, MILLISECONDS, MINUTES, SECONDS}

trait ReasonableWaits {

  val ThirtySeconds: FiniteDuration = Duration(30, SECONDS)
  val TenSeconds: FiniteDuration = Duration(10, SECONDS)
  val OneMinute: FiniteDuration = Duration(1, MINUTES)
  val FiveMinutes: FiniteDuration = Duration(5, MINUTES)
  val TenMilliSeconds: FiniteDuration = Duration(10, MILLISECONDS)
  val OneHundredMilliSeconds: FiniteDuration = Duration(100, MILLISECONDS)
}
