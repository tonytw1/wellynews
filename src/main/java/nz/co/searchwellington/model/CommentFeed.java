package nz.co.searchwellington.model;

import java.util.Date;
import java.util.List;

// TODO CommentFeeds should subclass Feeds
public class CommentFeed {
    
    int id;
    String url;
    List<Comment> comments;
    Newsitem newsitem;
    Date lastRead;
   
    
    public CommentFeed() {
    }
    
    public CommentFeed(int id, String url, List<Comment> comments, Newsitem newsitem, Date lastRead) {		
		this.id = id;
		this.url = url;
		this.comments = comments;
		this.newsitem = newsitem;
		this.lastRead = lastRead;		
	}
    
	public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public Newsitem getNewsitem() {
        return newsitem;
    }

    public void setNewsitem(Newsitem newsitem) {
        this.newsitem = newsitem;
    }
    
	public Date getLastRead() {
		return lastRead;
	}

	public void setLastRead(Date lastRead) {
		this.lastRead = lastRead;
	}
	
}
