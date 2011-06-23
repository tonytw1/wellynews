package nz.co.searchwellington.repositories;

import nz.co.searchwellington.model.frontend.FrontendResource;

import org.apache.solr.common.SolrDocument;

public interface ResourceHydrator {

	public FrontendResource hydrateResource(SolrDocument result);

}