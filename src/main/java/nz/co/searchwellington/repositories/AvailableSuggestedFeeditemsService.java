package nz.co.searchwellington.repositories;

import java.util.Iterator;
import java.util.List;

import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.model.Suggestion;
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class AvailableSuggestedFeeditemsService {
	
	private RssfeedNewsitemService rssfeedNewsitemService;
	
	@Autowired
	public AvailableSuggestedFeeditemsService(RssfeedNewsitemService rssfeedNewsitemService) {
		this.rssfeedNewsitemService = rssfeedNewsitemService;
	}
	
	public List<FrontendFeedNewsitem> getAvailableSuggestedFeeditems(List<Suggestion> bareSuggestions, int maxItems) {
		final List<FrontendFeedNewsitem> suggestions = Lists.newArrayList();
		Iterator<Suggestion> bareSuggestionsIterator = bareSuggestions.iterator();		
		while (suggestions.size() < maxItems && bareSuggestionsIterator.hasNext()) {
			Suggestion suggestion = bareSuggestionsIterator.next();
			if (suggestion.getFeed() != null) {
				FrontendFeedNewsitem feednewsitem = rssfeedNewsitemService.getFeedNewsitemByUrl(suggestion.getFeed(), suggestion.getUrl());
				if (feednewsitem != null) {
					suggestions.add(feednewsitem);
				}
			}
		}
		return suggestions;
	}

}
