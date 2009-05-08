package nz.co.searchwellington.widgets;


import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ResourceRepository;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
import org.apache.log4j.Logger;


public class PublisherSelectFactory {

    
    Logger log = Logger.getLogger(PublisherSelectFactory.class);
    
    final int MAXIMUM_TITLE_LENGTH = 45;

    
    private ResourceRepository resourceDAO;
    
    
        
    public PublisherSelectFactory(ResourceRepository resourceDAO) {
        this.resourceDAO = resourceDAO;
    }

    
    public Select createPublisherSelectWithNoCounts(Website selectedPublisher, boolean showBroken) throws IOException {        
        return build("publisher", selectedPublisher, showBroken, false);       
    }
    
    
    public Select createPublisherSelectWithCounts(Website selectedPublisher, boolean showBroken) throws IOException {        
        return build("publisher", selectedPublisher, showBroken, true);       
    }


    
    private Select build(String selectName, Website selectedPublisher, boolean showBroken, boolean showCounts) throws IOException {
        Select publisherSelect = new Select(selectName);
        publisherSelect.setID(selectName);

        Option noOption = new Option("");
        noOption.setFilterState(true);
        noOption.addElement("No Publisher");
        publisherSelect.addElement(noOption);
        
        final boolean mustHaveNewsitems = showCounts;
        List<Option> options = createOptions(showBroken, showCounts, mustHaveNewsitems, publisherSelect);
        
        for (Option option : options) {
            boolean isSelectedPublisher = selectedPublisher != null && option.getValue().equals(selectedPublisher.getUrlWords());
			if (isSelectedPublisher) {
                Option locallySelectedOption = (Option) option.clone();
                locallySelectedOption.setSelected(true);
                publisherSelect.addElement(locallySelectedOption);
            } else {
                publisherSelect.addElement(option);
            }
        }
        
        return publisherSelect;
    }


    // TODO Preformance; needs the urlwords field in the db.
    private List<Option> createOptions(boolean showBroken, boolean showCounts, boolean publishedOnly, Select publisherSelect) throws IOException {
        log.info("Creating publisher options; showBroken: " + showBroken + "; publishedOnly: " + publishedOnly);
        List<Option> options = new ArrayList<Option>();

        for (Object[] publisher : resourceDAO.getAllPublishers(showBroken, publishedOnly)) {
            Integer publisherId = (Integer) publisher[0];
            String publisherUrlWords = ((Website) resourceDAO.loadResourceById(publisherId)).getUrlWords();
            Option nextOption = new Option(publisherUrlWords);

            // Trim the titles to prevent the dropdown distorting the HTML.            
            String optionTitle = (String) publisher[1];
            if (optionTitle.length() > MAXIMUM_TITLE_LENGTH) {
                optionTitle = optionTitle.substring(0, MAXIMUM_TITLE_LENGTH) + "...";
            }
            optionTitle = StringEscapeUtils.escapeHtml(optionTitle);

            if (showCounts) {
                BigInteger articleCount = (BigInteger) publisher[2];
                if (articleCount.intValue() > 0) {
                    optionTitle = optionTitle + " (" + articleCount + ")";
                }
            }
            nextOption.addElement(optionTitle);
            options.add(nextOption);
        }

        return options;
    }
    
    
}
