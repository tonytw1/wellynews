package nz.co.searchwellington.repositories.solr;

import org.apache.solr.client.solrj.SolrQuery;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;

public class SolrKeywordQueryBuilder extends SolrQueryBuilder {
	
	
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
	
	
	public SolrQuery getSolrKeywordQueryNotTaggedByUser(String keywords, boolean showBroken, User user) {
		SolrQuery query = new SolrQuery();	
		query.setQuery(keywords);
		query.setQueryType("search");	
		if (!showBroken) {
			query.addFilterQuery("+httpStatus:200");
		}
		
		if (user != null) {
			query.addFilterQuery("-handTaggingUsers:" + user.getId());
		}
		return query;
	}
	
	
	public SolrQuery getSolrWebsiteKeywordQuery(String keywords, boolean showBroken, Tag tag) {
		SolrQuery query = getSolrKeywordQuery(keywords, showBroken, tag);
		query.addFilterQuery("+type:W");
		return query;
	}
	
	
	public SolrQuery getSolrNewsitemKeywordQuery(String keywords, boolean showBroken, Tag tag) {
		SolrQuery query = getSolrKeywordQuery(keywords, showBroken, tag);
		query.addFilterQuery("+type:N");
		return query;
	}
	
}
