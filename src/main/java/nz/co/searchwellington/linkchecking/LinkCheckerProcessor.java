package nz.co.searchwellington.linkchecking;

import nz.co.searchwellington.model.Resource;

public interface LinkCheckerProcessor {

	public void process(Resource checkResource, String pageContent);
	
}
