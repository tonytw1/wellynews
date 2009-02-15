package nz.co.searchwellington.model;

import java.util.Date;
import java.util.List;

public interface CommentFeed {
    
    public int getId();
    public void setId(int id);

    public String getUrl();

    public void setUrl(String url);

    public List<Comment> getComments();

    public void setComments(List<Comment> comments);
  
    public Date getLastRead();

    public void setLastRead(Date lastRead);
  
}