package nz.co.searchwellington.repositories;

import nz.co.searchwellington.model.Resource;

import org.apache.solr.common.SolrDocument;

public interface ResourceHydrator {

	public Resource hydrateResource(SolrDocument result);

}