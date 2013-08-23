package nz.co.searchwellington.model.frontend;

import java.io.Serializable;

public class FrontendImage implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String url;

	public FrontendImage() {
	}
	
	public FrontendImage(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return "FrontendImage [url=" + url + "]";
	}
	
}
