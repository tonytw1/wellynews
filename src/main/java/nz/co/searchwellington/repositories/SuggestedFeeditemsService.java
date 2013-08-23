package nz.co.searchwellington.repositories;

import java.util.List;

import nz.co.searchwellington.feeds.FeedItemLocalCopyDecorator;
import nz.co.searchwellington.model.FrontendFeedNewsitem;
import nz.co.searchwellington.model.Suggestion;
import nz.co.searchwellington.model.frontend.FrontendNewsitem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SuggestedFeeditemsService {

	private SuggestionDAO suggestionDAO;
	private AvailableSuggestedFeeditemsService availableSuggestedFeeditemsService;
	private FeedItemLocalCopyDecorator feedItemLocalCopyDecorator;
	
	@Autowired
	public SuggestedFeeditemsService(
			SuggestionDAO suggestionDAO,
			AvailableSuggestedFeeditemsService availableSuggestedFeeditemsService,
			FeedItemLocalCopyDecorator feedItemLocalCopyDecorator) {
		this.suggestionDAO = suggestionDAO;
		this.availableSuggestedFeeditemsService = availableSuggestedFeeditemsService;
		this.feedItemLocalCopyDecorator = feedItemLocalCopyDecorator;
	}
	
	public List<FrontendNewsitem> getSuggestionFeednewsitems(int maxItems) {
		List<Suggestion> bareSuggestions = suggestionDAO.getSuggestions(maxItems);
		List<FrontendFeedNewsitem> suggestedFeeditems = availableSuggestedFeeditemsService.getAvailableSuggestedFeeditems(bareSuggestions, maxItems);
		return feedItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(suggestedFeeditems);
	}
	
}
