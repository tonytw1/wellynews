package nz.co.searchwellington.repositories.solr;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;

import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.stereotype.Component;

// TODO why isn't this folded into the main query builder
@Component
public class SolrKeywordQueryBuilder extends SolrQueryBuilder {
	
	public SolrKeywordQueryBuilder() {
	}
	
	public SolrQuery getSolrKeywordQuery(String keywords, boolean showBroken, Tag tag) {
		SolrQuery query = new SolrQuery();		
		query.setQuery(keywords);
		query.setQueryType("search");	
		if (!showBroken) {
			query.addFilterQuery("+httpStatus:200");
		}
		
		if (tag != null) {
			query.addFilterQuery("+tags:" + tag.getId());
		}
		return query;
	}
		
	public SolrQuery getSolrKeywordQueryNotTaggedByUser(String keywords, boolean showBroken, Tag tag, User user) {
		SolrQuery query = new SolrQuery();
		query.setQuery(keywords);
		query.setQueryType("search");
		if (!showBroken) {
			query.addFilterQuery("+httpStatus:200");
		}
		
		if (user != null && tag != null) {
			final String userTag = user.getId() + "\\:" + tag.getId();
			query.addFilterQuery("-handTaggingUserTags:" + userTag);
		}
		return query;
	}
		
	public SolrQuery getSolrWebsiteKeywordQuery(String keywords, boolean showBroken, Tag tag, int startIndex, int maxItems) {
		SolrQuery query = getSolrKeywordQuery(keywords, showBroken, tag);
		query.setStart(startIndex);
		query.setRows(maxItems);
		query.addFilterQuery("+type:W");
		return query;
	}
		
	public SolrQuery getSolrNewsitemKeywordQuery(String keywords, boolean showBroken, Tag tag) {
		SolrQuery query = getSolrKeywordQuery(keywords, showBroken, tag);		
		query.addFilterQuery("+type:N");
		return query;
	}
	
}
