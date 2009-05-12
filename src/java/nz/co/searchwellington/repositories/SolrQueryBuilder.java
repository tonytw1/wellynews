package nz.co.searchwellington.repositories;

import java.util.Set;

import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Website;

import org.apache.solr.client.solrj.SolrQuery;

public class SolrQueryBuilder {
		
	private StringBuilder sb;
	
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
		}
		return this;
	}
	
	public SolrQueryBuilder type(String type) {
		sb.append(" +type:" + type);		
		return this;
	}
	

	public SolrQuery toQuery() {
		SolrQuery query = new SolrQuery(sb.toString().trim());
		return query;
		
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
	
}
