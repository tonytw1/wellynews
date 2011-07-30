package nz.co.searchwellington.repositories;

import static org.junit.Assert.assertEquals;

import java.util.List;

import nz.co.searchwellington.feeds.RssfeedNewsitemService;
import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.FrontendFeedNewsitem;
import nz.co.searchwellington.model.Suggestion;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class SuggestedFeeditemsServiceTest {

	@Mock SuggestionRepository suggestionDAO;
	@Mock RssfeedNewsitemService rssfeedNewsitemService;
	@Mock AvailableSuggestedFeeditemsService availableSuggestedFeeditemsService;

	@Mock List<Suggestion> allSuggestions;
	@Mock List<FeedNewsitem> availableSuggestedFeedItems;
	@Mock List<FrontendFeedNewsitem> frontendFeednewsitems;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void shouldReturnAvailableSuggestedFeeditemsWrappedWithLocalCopyAndSuppressionInformation() throws Exception {				
		Mockito.when(suggestionDAO.getSuggestions(3)).thenReturn(allSuggestions);
		Mockito.when(availableSuggestedFeeditemsService.getAvailableSuggestedFeeditems(allSuggestions, 3)).thenReturn(availableSuggestedFeedItems);
		Mockito.when(rssfeedNewsitemService.addSupressionAndLocalCopyInformation(availableSuggestedFeedItems)).thenReturn(frontendFeednewsitems);
		SuggestedFeeditemsService service = new SuggestedFeeditemsService(suggestionDAO, availableSuggestedFeeditemsService, rssfeedNewsitemService);
		
		assertEquals(frontendFeednewsitems, service.getSuggestionFeednewsitems(3));
	}
	
}
