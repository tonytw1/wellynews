package nz.co.searchwellington.widgets;

import nz.co.searchwellington.model.FeedAcceptancePolicy;

import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
import org.springframework.stereotype.Component;

@Component
public class AcceptanceWidgetFactory {
             
    public String createAcceptanceSelect(FeedAcceptancePolicy feedAcceptancePolicy) {
        Select acceptanceSelect = new Select("acceptance");
                
        for (FeedAcceptancePolicy policy : FeedAcceptancePolicy.values()) {      
            Option policyOption = new Option(policy.toString(), policy.getLabel());
            policyOption.setFilterState(true);
            policyOption.addElement(policy.getLabel());
            if (policy == feedAcceptancePolicy) {
                policyOption.setSelected(true);
            }
            acceptanceSelect.addElement(policyOption);
        }
        
        return acceptanceSelect.toString();  
    }
    
}
