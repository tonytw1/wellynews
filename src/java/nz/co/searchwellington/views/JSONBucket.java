package nz.co.searchwellington.views;

import java.util.List;

import nz.co.searchwellington.model.RssFeedable;

public class JSONBucket {

	private Integer totalItems;
	private Integer showingFrom;
	private Integer showingTo;

	private List<RssFeedable> newsitems;

	public Integer getTotalItems() {
		return totalItems;
	}
	
	public void setTotalItems(Integer totalItems) {
		this.totalItems = totalItems;
	}
	
	public Integer getShowingFrom() {
		return showingFrom;
	}

	public void setShowingFrom(Integer showingFrom) {
		this.showingFrom = showingFrom;
	}

	public Integer getShowingTo() {
		return showingTo;
	}

	public void setShowingTo(Integer showingTo) {
		this.showingTo = showingTo;
	}

	public List<RssFeedable> getNewsitems() {
		return newsitems;
	}

	public void setNewsitems(List<RssFeedable> newsitems) {
		this.newsitems = newsitems;
	}

}
