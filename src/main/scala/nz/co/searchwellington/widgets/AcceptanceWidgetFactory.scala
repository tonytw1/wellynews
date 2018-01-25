package nz.co.searchwellington.widgets

import nz.co.searchwellington.model.FeedAcceptancePolicy
import org.apache.ecs.html.Option
import org.apache.ecs.html.Select
import org.springframework.stereotype.Component

@Component class AcceptanceWidgetFactory {
  def createAcceptanceSelect(feedAcceptancePolicy: FeedAcceptancePolicy): String = {
    val acceptanceSelect: Select = new Select("acceptance")
    for (policy <- FeedAcceptancePolicy.values) {
      val policyOption: Option = new Option(policy.getLabel, policy.toString)
      policyOption.setFilterState(true)
      policyOption.addElement(policy.getLabel)
      if (policy eq feedAcceptancePolicy) {
        policyOption.setSelected(true)
      }
      acceptanceSelect.addElement(policyOption)
    }
    return acceptanceSelect.toString
  }
}