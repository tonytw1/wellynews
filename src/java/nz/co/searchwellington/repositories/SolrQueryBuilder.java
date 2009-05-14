package nz.co.searchwellington.repositories;

import java.util.Set;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;

import org.apache.solr.client.solrj.SolrQuery;

public class SolrQueryBuilder {
		
	private StringBuilder sb;
	private Integer startIndex;
	private Integer maxItems;
	
	public SolrQueryBuilder() {	
		this.sb = new StringBuilder();
		this.startIndex = null;
		this.maxItems = null;
	}

	public SolrQueryBuilder tag(Tag tag) {
		sb.append(" +tags:" + tag.getId());
		return this;
	}

	public SolrQueryBuilder showBroken(boolean showBroken) {
		if (!showBroken) {
			sb.append(" +httpStatus:200");			
		}
		return this;
	}
	
	public SolrQueryBuilder type(String type) {
		sb.append(" +type:" + type);		
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

	public SolrQueryBuilder publisher(Website publisher) {
		if (publisher != null) {
			sb.append(" +publisher:" + publisher.getId());			
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
	
	public SolrQueryBuilder month(String monthString) {
		if (monthString != null) {
			sb.append(" +month:" + monthString);
		}
		return this;
	}
	
	public SolrQuery toQuery() {
		SolrQuery query = new SolrQuery(sb.toString().trim());
		if (startIndex != null) {
			query.setStart(startIndex);
		}
		if (maxItems != null) {
			query.setRows(maxItems);
		}		
		return query;		
	}

	
}
