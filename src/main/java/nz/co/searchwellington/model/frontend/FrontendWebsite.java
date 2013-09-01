package nz.co.searchwellington.model.frontend;

public class FrontendWebsite extends FrontendResource {
	
	private static final long serialVersionUID = 1L;
	
	private String urlWords;
	
	public void setUrlWords(String urlWords) {
		this.urlWords = urlWords;
	}
	
	public String getUrlWords() {
		return urlWords;
	}

	@Override
	public String toString() {
		return "FrontendWebsite [urlWords=" + urlWords + ", getUrlWords()="
				+ getUrlWords() + ", getAuthor()=" + getAuthor()
				+ ", getDate()=" + getDate() + ", getDescription()="
				+ getDescription() + ", getHandTags()=" + getHandTags()
				+ ", getHeadline()=" + getHeadline() + ", getHttpStatus()="
				+ getHttpStatus() + ", getId()=" + getId() + ", getImageUrl()="
				+ getImageUrl() + ", getLatLong()=" + getLatLong()
				+ ", getLiveTime()=" + getLiveTime() + ", getLocation()="
				+ getLocation() + ", getName()=" + getName() + ", getOwner()="
				+ getOwner() + ", getPlace()=" + getPlace() + ", getTags()="
				+ getTags() + ", getType()=" + getType() + ", getUrl()="
				+ getUrl() + ", getWebUrl()=" + getWebUrl() + ", isHeld()="
				+ isHeld() + "]";
	}
	
}
