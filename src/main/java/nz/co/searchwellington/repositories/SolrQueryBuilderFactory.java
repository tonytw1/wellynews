package nz.co.searchwellington.repositories;

import org.springframework.stereotype.Component;

import nz.co.searchwellington.repositories.solr.SolrQueryBuilder;

@Component
public class SolrQueryBuilderFactory {

	public SolrQueryBuilder makeNewBuilder() {
		return new SolrQueryBuilder();
	}
	
}
