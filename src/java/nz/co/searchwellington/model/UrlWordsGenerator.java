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
		if (newsitem.getDate() != null) {
			uri.append("/" + dateFormatter.formatDate(newsitem.getDate(), "yyyy"));
			uri.append("/" + dateFormatter.formatDate(newsitem.getDate(), "MMM").toLowerCase());
			uri.append("/" + dateFormatter.formatDate(newsitem.getDate(), "dd"));
			uri.append("/" + makeUrlWordsFromName(newsitem.getName()));
			return uri.toString();
		}
		return null;
	}
	
}
