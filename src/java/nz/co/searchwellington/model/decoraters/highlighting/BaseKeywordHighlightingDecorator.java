package nz.co.searchwellington.model.decoraters.highlighting;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.Set;

import nz.co.searchwellington.model.DiscoveredFeed;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.repositories.LuceneAnalyzer;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;

import com.sun.syndication.feed.synd.SyndEntry;

public abstract class BaseKeywordHighlightingDecorator implements Resource {

    Logger log = Logger.getLogger(KeywordHighlightingNewsitemDecorator.class);

      
    Resource resource;
    
    protected final Query luceneQuery;
    protected final LuceneAnalyzer analyzer;

    
    public BaseKeywordHighlightingDecorator(final Query luceneQuery, final LuceneAnalyzer analyzer, Resource resource) {      
        this.luceneQuery = luceneQuery;
        this.analyzer = analyzer;
        this.resource = resource;
    }


    protected final String doKeywordHighlighting(String fieldname, String input) {
        Formatter formatter = new SimpleHTMLFormatter("<span class=\"highlight\">", "</span>");
        Highlighter highlighter = new Highlighter(formatter, new QueryScorer(luceneQuery));

        // TODO what is the 10000 for?
        Fragmenter fragmenter = new SimpleFragmenter(10000);
        highlighter.setTextFragmenter(fragmenter);

        TokenStream bodyTokenStream = analyzer.tokenStream(fieldname, new StringReader(input));
        try {
            String fragement = highlighter.getBestFragments(bodyTokenStream, input, 1, "");
            log.debug("Hightlighting: " + fieldname + ", " + input);
            if (!fragement.equals("")) {
                input = fragement;
            }
        } catch (IOException e) {
            log.error("Error while hightlighting field '" + fieldname + "' with content: " + input, e);
            return input;
        }
        return input;
    }
    
    

    final public String getName() {
        return doKeywordHighlighting("name", resource.getName());
    }
    
    final public String getDescription() {
        return doKeywordHighlighting("description", resource.getDescription());
    }
    
    
    

    final public void addTag(Tag tag) {
        resource.addTag(tag);
    }


    final public Date getDate() {
        return resource.getDate();
    }


  
    final public Set<DiscoveredFeed> getDiscoveredFeeds() {
        return resource.getDiscoveredFeeds();
    }


    final public int getHttpStatus() {
        return resource.getHttpStatus();
    }


    public int getId() {
        return resource.getId();
    }


    final public Date getLastChanged() {
        return resource.getLastChanged();
    }


    final public Date getLastScanned() {
        return resource.getLastScanned();
    }


   
    final public SyndEntry getRssItem() {
        return resource.getRssItem();
    }


    final public Set<Tag> getTags() {
        return resource.getTags();
    }


    final public String getType() {
        return resource.getType();
    }


    // TODO
    final public String getUrl() {
        try {
            return resource.getUrl();
        } catch (Exception e) {
            log.info("getUrl", e);
            return null;
        }
    }


    final public void setDate(Date date) {
        resource.setDate(date);
    }


    final public void setDescription(String description) {
        resource.setDescription(description);
    }


    final public void setDiscoveredFeeds(Set<DiscoveredFeed> discoveredFeeds) {
        resource.setDiscoveredFeeds(discoveredFeeds);
    }


    final public void setHttpStatus(int httpStatus) {
        resource.setHttpStatus(httpStatus);
    }


    public void setId(int id) {
        resource.setId(id);
    }


    final public void setLastChanged(Date lastChanged) {
        resource.setLastChanged(lastChanged);
    }


    final public void setLastScanned(Date lastScanned) {
        resource.setLastScanned(lastScanned);
    }


    final public void setName(String name) {
        resource.setName(name);
    }


    final public void setTags(Set<Tag> tags) {
        resource.setTags(tags);
    }


    final public void setUrl(String url) {
        resource.setUrl(url);
    }


    public Date getLiveTime() {
        // TODO Auto-generated method stub
        return null;
    }


    public void setLiveTime(Date time) {
        // TODO Auto-generated method stub
        
    }


	public void setTechnoratiCount(int technoratiCount) {
	}


	public int getTechnoratiCount() {
		return resource.getTechnoratiCount();
	}


	public void setGeocode(Geocode geocode) {
	}


	public Geocode getGeocode() {
		return resource.getGeocode();
	}
    
    
    
         
}
