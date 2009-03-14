package nz.co.searchwellington.model.decoraters.highlighting;

import java.util.Set;

import nz.co.searchwellington.model.CalendarFeed;
import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Watchlist;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.LuceneAnalyzer;

import org.apache.lucene.search.Query;

public class KeywordHighlightingWebsiteDecorator extends BaseKeywordHighlightingDecorator implements Website {
    
      
  
    public KeywordHighlightingWebsiteDecorator(Website website, Query luceneQuery, LuceneAnalyzer analyzer) {
        super(luceneQuery, analyzer, website);           
    }
    
 
    public Set<Feed> getFeeds() {
        return ((Website) resource).getFeeds();
    }
    
    public Set<Resource> getNewsitems() {
        return ((Website) resource).getNewsitems();
    }
    
    public Set<Watchlist> getWatchlist() {
        return ((Website) resource).getWatchlist();
    }


    public Set<CalendarFeed> getCalendars() {
        return ((Website) resource).getCalendars();
    }

    public void setCalendars(Set<CalendarFeed> calendars) {
        ((Website) resource).setCalendars(calendars);        
    }
    
}
