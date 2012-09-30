package nz.co.searchwellington.linkchecking;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import nz.co.searchwellington.model.Resource;

@Component
public class FirstLiveTimeSetter implements LinkCheckerProcessor {

	public void process(Resource checkResource, String pageContent) {
		boolean goneLive = checkResource.getHttpStatus() == 200 && checkResource.getLiveTime() == null;
		if (goneLive) {
		    checkResource.setLiveTime(new DateTime().toDate());                                 
		}
	}

}
