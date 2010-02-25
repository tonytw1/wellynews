package nz.co.searchwellington.widgets;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.model.PublisherContentCount;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.ContentRetrievalService;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
import org.apache.log4j.Logger;


public class PublisherSelectFactory {

    
    Logger log = Logger.getLogger(PublisherSelectFactory.class);
    
    final int MAXIMUM_TITLE_LENGTH = 45;

    
    private ContentRetrievalService contentRetrievalService;
    
            
    public PublisherSelectFactory(ContentRetrievalService resourceDAO) {
        this.contentRetrievalService = resourceDAO;
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


    private List<Option> createOptions(boolean showBroken, boolean showCounts, boolean publishedOnly, Select publisherSelect) throws IOException {
        log.info("Creating publisher options; showBroken: " + showBroken + "; publishedOnly: " + publishedOnly);
        List<Option> options = new ArrayList<Option>();
        
        for (PublisherContentCount publisherSummary : contentRetrievalService.getAllPublishersWithNewsitemCounts(publishedOnly)) {
        	Website publisher = publisherSummary.getPublisher();
            if (publisher.getUrlWords() != null) {
            	Option nextOption = new Option(publisher.getUrlWords());            	         
            	String optionTitle = trimAndEscapeTitle(publisher.getName());
            	if (showCounts) {
            		int articleCount = publisherSummary.getCount();
            		if (articleCount > 0) {
            			optionTitle = optionTitle + " (" + articleCount + ")";
            		}
            	}
            	nextOption.addElement(optionTitle);
            	options.add(nextOption);
            }
        }
        return options;
    }


	private String trimAndEscapeTitle(String title) {
		if (title.length() > MAXIMUM_TITLE_LENGTH) {
			title = title.substring(0, MAXIMUM_TITLE_LENGTH) + "...";
		}
		title = StringEscapeUtils.escapeHtml(title);
		return title;
	}
    
    
}
