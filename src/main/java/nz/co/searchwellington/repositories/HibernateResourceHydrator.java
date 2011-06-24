package nz.co.searchwellington.repositories;

import nz.co.searchwellington.model.frontend.FrontendResource;

import org.apache.solr.common.SolrDocument;

public class HibernateResourceHydrator implements ResourceHydrator {
	
	private ResourceRepository resourceDAO;

	public HibernateResourceHydrator(ResourceRepository resourceDAO) {
		this.resourceDAO = resourceDAO;
	}

	@Override
	public FrontendResource hydrateResource(SolrDocument result) {
		final int resourceId = Integer.parseInt((String) result.getFieldValue("id"));
		return resourceDAO.loadResourceById(resourceId);
	}
	
}
