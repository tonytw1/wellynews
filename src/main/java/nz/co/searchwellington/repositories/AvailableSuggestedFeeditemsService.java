package nz.co.searchwellington.repositories;

import java.util.Iterator;
import java.util.List;

import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.Suggestion;

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
	
	public List<FeedNewsitem> getAvailableSuggestedFeeditems(List<Suggestion> bareSuggestions, int maxItems) {
		final List<FeedNewsitem> suggestions = Lists.newArrayList();
		Iterator<Suggestion> bareSuggestionsIterator = bareSuggestions.iterator();		
		while (suggestions.size() < maxItems && bareSuggestionsIterator.hasNext()) {
			Suggestion suggestion = bareSuggestionsIterator.next();
			if (suggestion.getFeed() != null) {
				FeedNewsitem feednewsitem = rssfeedNewsitemService.getFeedNewsitemByUrl(suggestion.getFeed(), suggestion.getUrl());
				if (feednewsitem != null) {
					suggestions.add(feednewsitem);
				}
			}
		}
		return suggestions;
	}

}
