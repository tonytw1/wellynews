package nz.co.searchwellington.views;

import java.util.List;

import nz.co.searchwellington.model.RssFeedable;

public class JSONBucket {

	private List<RssFeedable> newsitems;
	
	public List<RssFeedable> getNewsitems() {
		return newsitems;
	}

	public void setNewsitems(List<RssFeedable> newsitems) {
		this.newsitems = newsitems;
	}

}
