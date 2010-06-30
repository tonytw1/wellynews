package nz.co.searchwellington.model;

import nz.co.searchwellington.dates.DateFormatter;


public class UrlWordsGenerator {

	public static String makeUrlWordsFromName(String name) {
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
	
	public static String markUrlForNewsitem(Newsitem newsitem) {
		StringBuilder uri = new StringBuilder();		
		if (newsitem.getPublisher() != null) {
			uri.append("/" + makeUrlWordsFromName(newsitem.getPublisherName()));
		}
		DateFormatter dateFormatter = new DateFormatter();
		uri.append("/" + dateFormatter.formatDate(newsitem.getDate(), "yyyy"));
		uri.append("/" + dateFormatter.formatDate(newsitem.getDate(), "MM"));
		uri.append("/" + dateFormatter.formatDate(newsitem.getDate(), "d"));
		uri.append("/" + makeUrlWordsFromName(newsitem.getName()));
		return uri.toString();
	}
	
}
