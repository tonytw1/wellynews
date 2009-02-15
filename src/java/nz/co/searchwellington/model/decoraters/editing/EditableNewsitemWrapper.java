package nz.co.searchwellington.model.decoraters.editing;

import java.util.List;

import nz.co.searchwellington.model.Comment;
import nz.co.searchwellington.model.CommentFeed;
import nz.co.searchwellington.model.Newsitem;

public class EditableNewsitemWrapper extends EditablePublishedResourceWrapper implements Newsitem {
    
    Newsitem resource;
    
    public EditableNewsitemWrapper(Newsitem resource) {
        super(resource);
        this.resource = resource;
    }

   
    public CommentFeed getCommentFeed() {
        return resource.getCommentFeed();
        
    }

    public void setCommentFeed(CommentFeed commentFeed) {
        resource.setCommentFeed(commentFeed);
    }


    public List<Comment> getComments() {
        return resource.getComments();
    }


    public String getTwitterSubmitter() {
        return resource.getTwitterSubmitter();
    }


    public void setTwitterSubmitter(String submitter) {
        resource.setTwitterSubmitter(submitter);
    }


    public String getTwitterMessage() {
        return resource.getTwitterMessage();
    }


    public void setTwitterMessage(String twitterMessage) {
        resource.setTwitterMessage(twitterMessage);
    }
    
    
    

}
