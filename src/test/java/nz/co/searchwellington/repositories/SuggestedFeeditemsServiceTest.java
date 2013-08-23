package nz.co.searchwellington.repositories;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import nz.co.searchwellington.feeds.FeedItemLocalCopyDecorator;
import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.model.Suggestion;
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem;
import nz.co.searchwellington.model.frontend.FrontendNewsitem;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SuggestedFeeditemsServiceTest {

	@Mock SuggestionDAO suggestionDAO;
	@Mock RssfeedNewsitemService rssfeedNewsitemService;
	@Mock AvailableSuggestedFeeditemsService availableSuggestedFeeditemsService;
	@Mock FeedItemLocalCopyDecorator feedItemLocalCopyDecorator;

	@Mock List<Suggestion> allSuggestions;
	@Mock List<FrontendFeedNewsitem> availableSuggestedFeedItems;
	@Mock List<FrontendNewsitem> frontendFeednewsitems;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void shouldReturnAvailableSuggestedFeeditemsWrappedWithLocalCopyAndSuppressionInformation() throws Exception {
		when(suggestionDAO.getSuggestions(3)).thenReturn(allSuggestions);
		when(availableSuggestedFeeditemsService.getAvailableSuggestedFeeditems(allSuggestions, 3)).thenReturn(availableSuggestedFeedItems);
		when(feedItemLocalCopyDecorator.addSupressionAndLocalCopyInformation(availableSuggestedFeedItems)).thenReturn(frontendFeednewsitems);

		final SuggestedFeeditemsService service = new SuggestedFeeditemsService(
				suggestionDAO, availableSuggestedFeeditemsService,
				feedItemLocalCopyDecorator);

		assertEquals(frontendFeednewsitems, service.getSuggestionFeednewsitems(3));
	}
	
}
