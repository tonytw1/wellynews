package nz.co.searchwellington.repositories.solr;

import java.util.Set;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.SolrInputDocumentBuilder;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class SolrQueryBuilder {
		
	private static Logger log = Logger.getLogger(SolrQueryBuilder.class);
	
	private Integer startIndex;
	private Integer maxItems;
	private DateTime startDate;
	private DateTime endDate;
	private String type;
	private Boolean commented;
	private Website publisher;
	private Boolean geotagged;
	private User owner;
	private User taggingUser;
	private Integer twitterCount;
	private Tag tag;
	private Double latitude;
	private Double longitude;
	private Double radius;
	private Boolean showBroken;
	private Boolean publishedTypesOnly;
	private String pageUrl;
	private String order;
	
	public SolrQuery toQuery() {
		final StringBuilder sb = new StringBuilder();
		if (type != null) {	
			sb.append(" +type:" + type);
		}
		if (publisher != null) {
			sb.append(" +" + SolrInputDocumentBuilder.PUBLISHER_NAME + ":\"" + publisher.getName() + "\"");
		}
		if (twitterCount != null) {
			sb.append(" +twitterCount:[" + twitterCount + " TO *]");
		}
		if (commented != null && commented) {
			sb.append(" +commented:1");		
		}
		if (geotagged != null && geotagged) {
			sb.append(" +geotagged:true");
		}
		if (taggingUser != null) {
			sb.append(" +handTaggingUsers:" + taggingUser.getId());
		}
		if (owner != null) {
			sb.append(" +owner:" + owner.getId());
		}
		if (tag != null) {
			sb.append(" +tags:" + tag.getId());
		}
		if (pageUrl != null) {
			sb.append(" +pageUrl:'" + pageUrl + "'");
		}
		
		if (showBroken != null && !showBroken) {
			sb.append(" +httpStatus:200");
			sb.append(" -embargoedUntil:[NOW TO *]");
			sb.append(" -held:true");
		}
		
		String queryString = sb.toString().trim();		
		if (startDate != null && endDate != null) {
			final DateTimeFormatter dateFormatter = ISODateTimeFormat.dateTime();
			queryString = queryString + " +date:[" + dateFormatter.print(startDate) + " TO " + dateFormatter.print(endDate) + "]";
		}
		
		final SolrQuery query = new SolrQuery(queryString);
		if (startIndex != null) {
			query.setStart(startIndex);
		}
		if (maxItems != null) {
			query.setRows(maxItems);
		}
		
		if (latitude != null && longitude != null && radius != null) {
			query.setFilterQueries("{!geofilt}");
			query.setParam("sfield", "position");
			query.setParam("pt", latitude + "," + longitude);
			query.setParam("d", Double.toString(radius));		
		}
		
		if (publishedTypesOnly != null && publishedTypesOnly) {
			sb.append(" +type:[F TO N]");
		}
				
		if (order != null && order.equals("dateDescending")) {			
			query.setSortField("date", ORDER.desc);
			query.addSortField("id", ORDER.desc);
		}
		if (order != null && order.equals("title")) {
			query.setSortField("titleSort", ORDER.asc);
		}
		if (order != null && order.equals("feedLatestItemDate")) {
			query.setSortField("feedLatestItemDate", ORDER.desc);
		}
		
		log.debug("Solr query: " + queryString);
		return query;		
	}
	
	public SolrQueryBuilder setDateDescendingOrder() {
		this.order = "dateDescending";
		return this;
	}

	public SolrQueryBuilder setTitleSortOrder() {
		this.order = "title";
		return this;
	}

	public SolrQueryBuilder setFeedLatestItemOrder() {	// TODO used by who?
		this.order = "feedLatestItemDate";
		return this;
	}
		
	@Deprecated // TODO make a straight database call
	public SolrQueryBuilder pageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
		return this;
	}
	
	public SolrQueryBuilder allPublishedTypes() {
		this.publishedTypesOnly = true;
		return this;
	}
	
	public SolrQueryBuilder showBroken(boolean showBroken) {
		this.showBroken = showBroken;	
		return this;
	}
	
	public SolrQueryBuilder near(double latitude, double longitude, double radius) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.radius = radius;
		return this;
	}

	public SolrQueryBuilder tag(Tag tag) {
		this.tag = tag;
		return this;
	}
	
	public SolrQueryBuilder type(String type) {
		this.type = type;
		return this;
	}
	
	public SolrQueryBuilder tags(Set<Tag> tags) {
		for (Tag tag : tags) {
			this.tag(tag);
		}
		return this;
	}

	public SolrQueryBuilder commented(boolean commented) {
		this.commented = commented;
		return this;
	}

	public SolrQueryBuilder dateRange(int daysAgo) {
		this.endDate = DateTime.now();
		this.startDate = DateTime.now().minus(daysAgo);
		return this;
	}
	
	public SolrQueryBuilder publisher(Website publisher) {
		this.publisher = publisher;		
		return this;
	}

	public SolrQueryBuilder startIndex(int startIndex) {
		this.startIndex = startIndex;
		return this;
	}
	
	public SolrQueryBuilder maxItems(int maxItems) {
		this.maxItems = maxItems;
		return this;
	}
	
	public SolrQueryBuilder geotagged() {
		this.geotagged = true;
		return this;
	}
	
	public SolrQueryBuilder minTwitterCount(int count) {
		this.twitterCount = count;
		return this;
	}

	public SolrQueryBuilder taggingUser(User user) {
		this.taggingUser = user;
		return this;
	}

	public SolrQueryBuilder owningUser(User user) {
		this.owner = user;
		return this;
	}

	public SolrQueryBuilder startDate(DateTime startDate) {
		this.startDate = startDate.withZone(DateTimeZone.UTC);
		return this;
	}
	
	public SolrQueryBuilder endDate(DateTime endDate) {
		this.endDate = endDate.withZone(DateTimeZone.UTC);
		return this;
	}
	
}