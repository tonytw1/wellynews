package nz.co.searchwellington.repositories;

import nz.co.searchwellington.repositories.solr.SolrQueryBuilder;

public class SolrQueryBuilderFactory {

	public SolrQueryBuilder makeNewBuilder() {
		return new SolrQueryBuilder();
	}
	
}
