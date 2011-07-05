package nz.co.searchwellington.repositories.solr.indexing;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nz.co.searchwellington.model.Twit;

import org.apache.solr.common.SolrInputDocument;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class SolrTweetsHandlerTest {

	private SolrTweetsHandler solrTweetsHandler;
	
	private List<Twit> tweets;
	@Mock Twit firstTweet;
	@Mock Twit secondTweet;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		solrTweetsHandler = new SolrTweetsHandler();

		tweets = new ArrayList<Twit>();
		Mockito.when(firstTweet.getAuthor()).thenReturn("tonytw1");
		Mockito.when(firstTweet.getText()).thenReturn("Blah blah");
		tweets.add(firstTweet);
		
		Mockito.when(secondTweet.getAuthor()).thenReturn("someone");
		Mockito.when(secondTweet.getText()).thenReturn("Rant rant");
		tweets.add(secondTweet);
	}
	
	@Test
	public void newsitemTweetsShouldBeAddedToSolrIndex() throws Exception {
		SolrInputDocument solrInputDocument = new SolrInputDocument();
		
		solrInputDocument = solrTweetsHandler.processTweets(tweets, solrInputDocument);
		
		assertEquals(2, solrInputDocument.getFieldValue("twitter_count"));
		Collection<Object> tweetUsernames = solrInputDocument.getFieldValues("tweet_author");
		Collection<Object> tweetTexts = solrInputDocument.getFieldValues("tweet_text");
		
		assertEquals("[tonytw1, someone]", tweetUsernames.toString());
		assertEquals("[Blah blah, Rant rant]", tweetTexts.toString());
	}
	
}
