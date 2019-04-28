package nz.co.searchwellington.linkchecking;

import nz.co.searchwellington.model.Resource;
import org.joda.time.DateTime;

public interface LinkCheckerProcessor {

	public void process(Resource checkResource, String pageContent, DateTime seen);
	
}
