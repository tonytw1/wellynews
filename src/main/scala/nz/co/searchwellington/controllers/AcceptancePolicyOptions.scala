package nz.co.searchwellington.controllers

import nz.co.searchwellington.model.FeedAcceptancePolicy

trait AcceptancePolicyOptions {

  val acceptancePolicyOptions: Seq[String] = FeedAcceptancePolicy.values().map(_.name()).toSeq

}
