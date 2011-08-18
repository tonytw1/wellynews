package nz.co.searchwellington.model.frontend;

public class FrontendWebsiteImpl extends FrontendResourceImpl implements FrontendWebsite {
	
	private static final long serialVersionUID = 1L;
	
	private String urlWords;
	
	public void setUrlWords(String urlWords) {
		this.urlWords = urlWords;
	}
	
	public String getUrlWords() {
		return urlWords;
	}
	
}
