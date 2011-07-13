package nz.co.searchwellington.views;

import java.util.List;

public class JSONBucket {

	private Integer totalItems;
	private String description;
	private Integer showingFrom;
	private Integer showingTo;

	private List<JSONFeedItem> newsitems;

	public Integer getTotalItems() {
		return totalItems;
	}
	
	public void setTotalItems(Integer totalItems) {
		this.totalItems = totalItems;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public List<JSONFeedItem> getNewsitems() {
		return newsitems;
	}

	public void setNewsitems(List<JSONFeedItem> newsitems) {
		this.newsitems = newsitems;
	}

}
