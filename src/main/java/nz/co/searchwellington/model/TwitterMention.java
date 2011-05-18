package nz.co.searchwellington.model;

public class TwitterMention {

	private Newsitem newsitem;
	private Twit twit;
	
	
	public TwitterMention(Newsitem newsitem, Twit twit) {
		this.newsitem = newsitem;
		this.twit = twit;
	}


	public Newsitem getNewsitem() {
		return newsitem;
	}


	public void setNewsitem(Newsitem newsitem) {
		this.newsitem = newsitem;
	}


	public Twit getTwit() {
		return twit;
	}


	public void setTwit(Twit twit) {
		this.twit = twit;
	}
	
	
	
	
}
