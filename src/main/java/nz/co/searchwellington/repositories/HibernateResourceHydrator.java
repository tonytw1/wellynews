package nz.co.searchwellington.repositories;

import nz.co.searchwellington.model.frontend.FrontendResource;

import org.apache.solr.common.SolrDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HibernateResourceHydrator implements ResourceHydrator {
	
	private HibernateResourceDAO resourceDAO;

	@Autowired
	public HibernateResourceHydrator(HibernateResourceDAO resourceDAO) {
		this.resourceDAO = resourceDAO;
	}

	@Override
	public FrontendResource hydrateResource(SolrDocument result) {
		final int resourceId = Integer.parseInt((String) result.getFieldValue("id"));
		return resourceDAO.loadResourceById(resourceId);
	}
	
}
