package nz.co.searchwellington.controllers

import nz.co.searchwellington.model.FeedAcceptancePolicy

trait AcceptancePolicyOptions {

  val acceptancePolicyOptions = FeedAcceptancePolicy.values().map(_.name()).toSeq

}
