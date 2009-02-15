package nz.co.searchwellington.model;

import org.apache.log4j.Logger;
import org.apache.oro.text.perl.Perl5Util;


/**
 * Contains a snapshot of this resource. These snapshots are used to spot
 * changes in page content.
 * 
 * @author tony
 *
 */
public class SnapshotImpl implements Snapshot {

    Logger log = Logger.getLogger(SnapshotImpl.class);
    
    private String url;
    protected String body;

    
    public SnapshotImpl() {     
    }
  
    public SnapshotImpl(String url) {
        this.url = url;
    }
    
    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBody() {
        return body;
    }
    
  
    public void setBody(String newbody) {
        this.body = newbody;
    }
    
    
    
    
    /** PHP uses 32 characters of hex as a unique session id, 
     * and appends it to all links in a given page. We want to strip
     * this before trying to consider changes in the page.
     * 
     * @param content
     * @return content with 32 byte hex values stripped.
     */
    protected static String stripFalsePositives(String content) {
        String result = content;

        Perl5Util util = new Perl5Util();

        // Strip all html tags.
        result = util.substitute("s/<.*?>//g", result);
        return result;
    }
    
    
    
    
    // Snapshots are equal if the contents of the page is the same.
    // However, we preform some prefiltering before doing the comparsion.
    /* (non-Javadoc)
     * @see nz.co.searchwellington.model.Snapshot#contentMatches(nz.co.searchwellington.model.Snapshot)
     */
    public boolean contentMatches (Snapshot o) {
        log.debug("Snapshot.contentMatches() called.");
        boolean result = false;
          
        if (o.getBody() != null && this.getBody() != null) {
            log.debug("Applying false positive filters.");
            String before = stripFalsePositives(o.getBody());
            String after = stripFalsePositives(this.getBody());            
            log.debug("contentMatches comparing: " + before);
            log.debug("contentMatches to:" + after);
           
            result = before.equals(after);
        } else {      
            log.debug("One or more of the bodies been compared is null.");
            // does this work for nulls?
            if (this.getBody() == null && o.getBody() == null) {
                result = true;
            }
        }
        
        log.debug("Snapshot.contentMatches() returning: " + result);
        return result;
     }
    
}
