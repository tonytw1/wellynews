package nz.co.searchwellington.repositories.solr;

import org.apache.solr.client.solrj.SolrQuery;

import nz.co.searchwellington.repositories.SolrQueryBuilder;

public class SolrKeywordQueryBuilder extends SolrQueryBuilder {
	
	
	public SolrQuery getSolrKeywordQuery(String keywords, boolean showBroken) {
		SolrQuery query = new SolrQuery();	
		query.setQuery(keywords);
		query.setQueryType("search");	
		if (!showBroken) {
			query.setFilterQueries(" +httpStatus:200");
		}
		return query;
	}
	
	
	public SolrQuery getSolrWebsiteKeywordQuery(String keywords, boolean showBroken) {
		SolrQuery query = getSolrKeywordQuery(keywords, showBroken);
		query.setFilterQueries(" +type:W");
		return query;
	}
	
	
	public SolrQuery getSolrNewsitemKeywordQuery(String keywords, boolean showBroken) {
		SolrQuery query = getSolrKeywordQuery(keywords, showBroken);		
		query.setFilterQueries(" +type:N");	
		return query;
	}
	
}
