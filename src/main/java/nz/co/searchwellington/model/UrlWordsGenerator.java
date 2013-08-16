package nz.co.searchwellington.model;

import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.dates.DateFormatter;
import nz.co.searchwellington.model.frontend.FrontendNewsitem;

@Component
public class UrlWordsGenerator {
		
	public String makeUrlWordsFromName(String name) {
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
	
	public String markUrlForNewsitem(FrontendNewsitem newsitem) {
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
