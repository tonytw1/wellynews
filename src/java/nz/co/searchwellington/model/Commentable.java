package nz.co.searchwellington.model;

import java.util.List;

public interface Commentable {

    public CommentFeed getCommentFeed();
    public void setCommentFeed(CommentFeed commentFeed);
    
    public List<Comment> getComments();
}
