package nz.co.searchwellington.repositories;

import java.util.List;

import nz.co.searchwellington.feeds.CachingRssfeedNewsitemService;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.FrontendFeedNewsitem;
import nz.co.searchwellington.model.Suggestion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SuggestedFeeditemsService {

	private SuggestionDAO suggestionDAO;
	private AvailableSuggestedFeeditemsService availableSuggestedFeeditemsService;
	private CachingRssfeedNewsitemService rssfeedNewsitemService;

	@Autowired
	public SuggestedFeeditemsService(SuggestionDAO suggestionDAO, AvailableSuggestedFeeditemsService availableSuggestedFeeditemsService, CachingRssfeedNewsitemService rssfeedNewsitemService) {
		this.suggestionDAO = suggestionDAO;
		this.availableSuggestedFeeditemsService = availableSuggestedFeeditemsService;
		this.rssfeedNewsitemService = rssfeedNewsitemService;
	}
	
	public List<FrontendFeedNewsitem> getSuggestionFeednewsitems(int maxItems) {
		List<Suggestion> bareSuggestions = suggestionDAO.getSuggestions(maxItems);
		List<FeedNewsitem> suggestedFeeditems = availableSuggestedFeeditemsService.getAvailableSuggestedFeeditems(bareSuggestions, maxItems);
		return rssfeedNewsitemService.addSupressionAndLocalCopyInformation(suggestedFeeditems);
	}
	
}
