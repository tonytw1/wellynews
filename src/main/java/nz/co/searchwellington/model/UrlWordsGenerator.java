package nz.co.searchwellington.model;

import uk.co.eelpieconsulting.common.dates.DateFormatter;
import nz.co.searchwellington.model.frontend.FrontendNewsitem;

public class UrlWordsGenerator {
		
	public static String makeUrlWordsFromName(String name) {	// TODO why is this static?
		String urlWords = new String(name);		
		return urlWords.
			replaceAll("\\(.*?\\)", "").
			trim().
			replaceAll(" ", "-").
			replaceAll("\\s", "").
			replaceAll("[^\\w-]","").
			replaceAll("-+", "-").
			toLowerCase();
	}
	
	public static String markUrlForNewsitem(FrontendNewsitem newsitem) { // TODO why is this static?
		StringBuilder uri = new StringBuilder();
		if (newsitem.getPublisherName() != null) {
			uri.append("/" + makeUrlWordsFromName(newsitem.getPublisherName()));
		}
		
		final DateFormatter dateFormatter = new DateFormatter();
		if (newsitem.getDate() != null) {
			uri.append("/" + dateFormatter.yearMonthDayUrlStub(newsitem.getDate()));
			uri.append("/" + makeUrlWordsFromName(newsitem.getName()));
			return uri.toString();
		}
		return null;
	}
	
}
