package nz.co.searchwellington.repositories.solr;

import java.util.Set;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.SolrInputDocumentBuilder;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class SolrQueryBuilder {
		
	private static Logger log = Logger.getLogger(SolrQueryBuilder.class);
	
	private StringBuilder sb;	// TODO should be done in build method.
	private Integer startIndex;
	private Integer maxItems;
	private DateTime startDate;
	private DateTime endDate;
		
	public SolrQueryBuilder() {	
		this.sb = new StringBuilder();
	}

	public SolrQueryBuilder tag(Tag tag) {
		sb.append(" +tags:" + tag.getId());
		return this;
	}

	public SolrQueryBuilder showBroken(boolean showBroken) {
		if (!showBroken) {
			sb.append(" +httpStatus:200");
			sb.append(" -embargoedUntil:[NOW TO *]");
			sb.append(" -held:true");
		}
		return this;
	}
	
	
	public SolrQueryBuilder isBroken() {
		sb.append(" -httpStatus:200");
		return this;
	}
	
	
	public SolrQueryBuilder type(String type) {
		sb.append(" +type:" + type);		
		return this;
	}
	
	public SolrQueryBuilder allContentTypes() {
		sb.append(" +type:[F TO W]");		
		return this;
	}
	

	public SolrQueryBuilder allPublishedTypes() {
		sb.append(" +type:[F TO N]");		
		return this;
	}
		
	public SolrQueryBuilder tags(Set<Tag> tags) {
		for (Tag tag : tags) {
			this.tag(tag);
		}
		return this;
	}

	public SolrQueryBuilder commented(boolean commented) {
		if (commented) {
			sb.append(" +commented:1");		
		}
		return this;
	}

	public SolrQueryBuilder dateRange(int daysAgo) {
		sb.append(" +date:[NOW-" + daysAgo + "DAY TO NOW]");
		return this;
	}
	
	public SolrQueryBuilder publisher(Website publisher) {
		if (publisher != null) {
			sb.append(" +" + SolrInputDocumentBuilder.PUBLISHER_NAME + ":\"" + publisher.getName() + "\"");
		}
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
	
	@Deprecated // TODO implement as a prior date range
	public SolrQueryBuilder month(String monthString) {
		if (monthString != null) {
			sb.append(" +month:" + monthString);
		}
		return this;
	}
		
	public SolrQueryBuilder geotagged() {
		sb.append(" +geotagged:true");
		return this;
	}
	
	public SolrQuery toQuery() {
		String queryString = sb.toString().trim();		
		if (startDate != null && endDate != null) {
			final DateTimeFormatter dateFormatter = ISODateTimeFormat.dateTime();
			queryString = queryString + " +date:[" + dateFormatter.print(startDate) + " TO " + dateFormatter.print(endDate) + "]";
		}
		
		log.debug("Solr query: " + queryString);
		final SolrQuery query = new SolrQuery(queryString);
		if (startIndex != null) {
			query.setStart(startIndex);
		}
		if (maxItems != null) {
			query.setRows(maxItems);
		}
		return query;		
	}
	
	public SolrQuery toNewsitemsNearQuery(double latitude, double longitude, double radius, boolean showBroken, int startIndex, int maxItems) {		
		SolrQuery query = new SolrQueryBuilder().type("N").showBroken(showBroken).geotagged().startIndex(startIndex).maxItems(maxItems).toQuery();
		query.setFilterQueries("{!geofilt}");
		query.setParam("sfield", "position");
		query.setParam("pt", latitude + "," + longitude);
		query.setParam("d", Double.toString(radius));			
		return query;
	}
	
	public SolrQueryBuilder minTwitterCount(int count) {
		sb.append(" +twitterCount:[" + count + " TO *]");
		return this;
	}

	public SolrQueryBuilder taggingUser(User user) {
		sb.append(" +handTaggingUsers:" + user.getId());
		return this;
	}

	public SolrQueryBuilder pageUrl(String pageUrl) {
		sb.append(" +pageUrl:'" + pageUrl + "'");
		return this;
	}

	public SolrQueryBuilder owningUser(User user) {
		sb.append(" +owner:" + user.getId());
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