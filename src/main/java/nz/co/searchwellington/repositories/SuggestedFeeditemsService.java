package nz.co.searchwellington.repositories;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.Suggestion;

public class SuggestedFeeditemsService {

	private SuggestionRepository suggestionDAO;
	private RssfeedNewsitemService rssfeedNewsitemService;
	
	public SuggestedFeeditemsService(SuggestionRepository suggestionDAO, RssfeedNewsitemService rssfeedNewsitemService) {		
		this.suggestionDAO = suggestionDAO;
		this.rssfeedNewsitemService = rssfeedNewsitemService;
	}
	
	public List<FeedNewsitem> getSuggestionFeednewsitems(int maxItems) {
		List<FeedNewsitem> suggestions = new ArrayList<FeedNewsitem>();		
		List<Suggestion> bareSuggestions = suggestionDAO.getSuggestions(maxItems);
		Iterator<Suggestion> bareSuggresionsIterator = bareSuggestions.iterator();
		while (suggestions.size() < maxItems && bareSuggresionsIterator.hasNext()) {
			Suggestion suggestion = bareSuggresionsIterator.next();			
			if (suggestion.getFeed() != null) {
				FeedNewsitem feednewsitem = rssfeedNewsitemService.getFeedNewsitemByUrl(suggestion.getFeed(), suggestion.getUrl());
				if (feednewsitem != null) {
					suggestions.add(feednewsitem);
				}
			}
		}
		// TODO Needs local copy information
		return suggestions;
	}
	
}
