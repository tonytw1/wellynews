package nz.co.searchwellington.repositories;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexUpdateService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FrontendContentUpdater {
	
	private final static Logger log = Logger.getLogger(FrontendContentUpdater.class);

	private final ElasticSearchIndexUpdateService elasticSearchIndexUpdateService;
	
	@Autowired
	public FrontendContentUpdater(ElasticSearchIndexUpdateService elasticSearchIndexUpdateService) {
		this.elasticSearchIndexUpdateService = elasticSearchIndexUpdateService;
	}
	
	public void update(Resource updatedResource) {
		log.info("Updating elastic search record for resource: " + updatedResource.getName());
		elasticSearchIndexUpdateService.updateSingleContentItem(updatedResource);
	}

}
