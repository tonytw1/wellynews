package nz.co.searchwellington.repositories.solr;

import org.apache.solr.client.solrj.SolrQuery;

import nz.co.searchwellington.repositories.SolrQueryBuilder;

public class SolrKeywordQueryBuilder extends SolrQueryBuilder {
	
	public SolrQuery getSolrWebsiteKeywordQuery(String keywords, boolean showBroken) {
		SolrQuery query = new SolrQuery();	
		query.setQuery(keywords);
		query.setQueryType("search");
		query.setFilterQueries("+type:W");
		if (!showBroken) {
			query.setFilterQueries(" +httpStatus:200");
		}
		return query;
	}
	
	
	public SolrQuery getSolrNewsitemKeywordQuery(String keywords, boolean showBroken) {
		SolrQuery query = new SolrQuery();	
		query.setQuery(keywords);
		query.setQueryType("search");
		query.setFilterQueries("+type:N");	
		if (!showBroken) {
			query.setFilterQueries(" +httpStatus:200");
		}
		return query;
	}
	
}
