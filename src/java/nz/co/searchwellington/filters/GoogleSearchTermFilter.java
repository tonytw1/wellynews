package nz.co.searchwellington.filters;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

public class GoogleSearchTermFilter implements RequestAttributeFilter {
	
    public static final String SEARCH_TERM = "searchterm";
	static Logger log = Logger.getLogger(GoogleSearchTermFilter.class);

    private GoogleSearchTermExtractor searchTermExtractor;
    	
    public GoogleSearchTermFilter(GoogleSearchTermExtractor searchTermExtractor) {
		this.searchTermExtractor = searchTermExtractor;
	}
   
	public void filter(HttpServletRequest request) {
		final String referer = request.getHeader("Referer");
		if (referer != null) {				
			final String searchTerm = searchTermExtractor.extractSearchTerm(referer);
			if (searchTerm != null) {
				log.info("Referrer search term is: " + searchTerm);
				request.setAttribute(SEARCH_TERM, searchTerm);
			}			
		}		
	}

}
