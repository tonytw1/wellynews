package nz.co.searchwellington.repositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.FrontendFeedNewsitem;
import nz.co.searchwellington.model.Suggestion;

@Component
public class SuggestedFeeditemsService {

	private SuggestionRepository suggestionDAO;
	private AvailableSuggestedFeeditemsService availableSuggestedFeeditemsService;
	private RssfeedNewsitemService rssfeedNewsitemService;

	@Autowired
	public SuggestedFeeditemsService(SuggestionRepository suggestionDAO, AvailableSuggestedFeeditemsService availableSuggestedFeeditemsService, RssfeedNewsitemService rssfeedNewsitemService) {
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
