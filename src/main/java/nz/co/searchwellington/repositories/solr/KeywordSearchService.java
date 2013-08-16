package nz.co.searchwellington.repositories.solr;

import java.util.List;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.frontend.FrontendResource;

import org.elasticsearch.common.collect.Lists;
import org.springframework.stereotype.Component;

@Component
public class KeywordSearchService {

	public List<FrontendResource> getNewsitemsMatchingKeywords(String keywords, boolean showBroken, Tag tag, int startIndex, int maxNewsitems) {
		return Lists.newArrayList();	// TODO implement
	}
	
	public int getNewsitemsMatchingKeywordsCount(String keywords, boolean shouldShowBroken, Tag tag) {
		return 0;	// TODO implement
	}
	
	public List<FrontendResource> getWebsitesMatchingKeywords(String keywords, boolean showBroken, Tag tag, int startIndex, int maxItems) {
		return Lists.newArrayList();	// TODO implement

	}
	
	public List<FrontendResource> getResourcesMatchingKeywordsNotTaggedByUser(String keywords, boolean showBroken, User user, Tag tag) {
		return Lists.newArrayList();	// TODO implement
	}
	
}
