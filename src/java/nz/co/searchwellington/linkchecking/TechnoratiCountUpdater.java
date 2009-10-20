package nz.co.searchwellington.linkchecking;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.TechnoratiDAO;

import org.apache.log4j.Logger;


public class TechnoratiCountUpdater implements LinkCheckerProcessor {

    private static Logger log = Logger.getLogger(TechnoratiCountUpdater.class);

    private TechnoratiDAO technoratiDAO;
    
	protected TechnoratiCountUpdater(TechnoratiDAO technoratiDAO) {
		this.technoratiDAO = technoratiDAO;
	}


	public void process(Resource checkResource, String pageContent) {
		// TODO get exclude url from config.
		int technoratiCount = technoratiDAO.getTechnoratiLinkCount(checkResource.getUrl(), "http://www.wellington.gen.nz");
		log.info("Technorati count is: " + technoratiCount);
		checkResource.setTechnoratiCount(technoratiCount);
	}

}
