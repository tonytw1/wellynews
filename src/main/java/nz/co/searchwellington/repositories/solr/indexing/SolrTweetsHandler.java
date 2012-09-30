package nz.co.searchwellington.repositories.solr.indexing;

import java.util.List;

import nz.co.searchwellington.model.Twit;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Component;

@Component
public class SolrTweetsHandler {
	
	public SolrInputDocument processTweets(List<Twit> tweets, SolrInputDocument inputDocument) {
		inputDocument.addField("twitterCount", tweets.size());
		for (Twit tweet : tweets) {
			inputDocument.addField("tweet_author", tweet.getAuthor());
			inputDocument.addField("tweet_text", tweet.getText());
		}
		return inputDocument;
	}
	
}
