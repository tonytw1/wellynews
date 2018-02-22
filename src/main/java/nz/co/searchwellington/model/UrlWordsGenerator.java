package nz.co.searchwellington.model;

import nz.co.searchwellington.model.frontend.FrontendNewsitem;

import org.joda.time.DateTimeZone;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.dates.DateFormatter;

@Component
public class UrlWordsGenerator {
		
	public String makeUrlWordsFromName(String name) {
		if (name != null) {
			String urlWords = new String(name);
			return urlWords.
					replaceAll("\\(.*?\\)", "").
					trim().
					replaceAll(" ", "-").
					replaceAll("\\s", "").
					replaceAll("[^\\w-]", "").
					replaceAll("-+", "-").
					toLowerCase();
		} else {
			return null;
		}
	}
	
	public String makeUrlForNewsitem(FrontendNewsitem newsitem) {
		StringBuilder uri = new StringBuilder();
		if (newsitem.getPublisherName() != null) {
			uri.append("/" + makeUrlWordsFromName(newsitem.getPublisherName()));
		}
		
		final DateFormatter dateFormatter = new DateFormatter(DateTimeZone.UTC);
		if (newsitem.getDate() != null) {
			uri.append("/" + dateFormatter.yearMonthDayUrlStub(newsitem.getDate()));
			uri.append("/" + makeUrlWordsFromName(newsitem.getName()));
			return uri.toString();
		}
		return null;
	}
	
}
