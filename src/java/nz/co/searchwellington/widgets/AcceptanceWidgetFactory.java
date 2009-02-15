package nz.co.searchwellington.widgets;


import nz.co.searchwellington.model.FeedAcceptancePolicy;

import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
import org.apache.log4j.Logger;


public class AcceptanceWidgetFactory {
    
    Logger log = Logger.getLogger(AcceptanceWidgetFactory.class);
         
    public String createAcceptanceSelect(String acceptancePolicy) {
        Select acceptanceSelect = new Select("acceptance");
                
        for (FeedAcceptancePolicy policy : FeedAcceptancePolicy.values()) {      
            Option policyOption = new Option(policy.getName());
            policyOption.setFilterState(true);
            policyOption.addElement(policy.getLabel());
            if (policy.getName().equals(acceptancePolicy)) {
                policyOption.setSelected(true);
            }

            acceptanceSelect.addElement(policyOption);
        }
      
        return acceptanceSelect.toString();  
    }
    
}
