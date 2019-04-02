package nz.co.searchwellington

import scala.concurrent.duration.{Duration, MINUTES, SECONDS}

trait ReasonableWaits {

  val TenSeconds = Duration(10, SECONDS)
  val OneMinute = Duration(1, MINUTES)

}
