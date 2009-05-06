package nz.co.searchwellington.views;

import java.util.List;

import nz.co.searchwellington.model.RssFeedable;

public class JSONBucket {

	//private int totalItems;
	private List<RssFeedable> newsitems;

//	public int getTotalItems() {
//		return totalItems;
//	}
	
//	public void setTotalItems(int totalItems) {
//		this.totalItems = totalItems;
//	}

	public List<RssFeedable> getNewsitems() {
		return newsitems;
	}

	public void setNewsitems(List<RssFeedable> newsitems) {
		this.newsitems = newsitems;
	}

}
