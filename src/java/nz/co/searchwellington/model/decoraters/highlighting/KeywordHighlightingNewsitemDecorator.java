package nz.co.searchwellington.model.decoraters.highlighting;

import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.model.Comment;
import nz.co.searchwellington.model.CommentFeed;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Website;
import nz.co.searchwellington.repositories.LuceneAnalyzer;

import org.apache.log4j.Logger;
import org.apache.lucene.search.Query;

public class KeywordHighlightingNewsitemDecorator extends BaseKeywordHighlightingDecorator implements Newsitem {
     
    Logger log = Logger.getLogger(KeywordHighlightingNewsitemDecorator.class);
   

   
    public KeywordHighlightingNewsitemDecorator(Newsitem newsitem, Query luceneQuery, LuceneAnalyzer analyzer) {       
        super(luceneQuery, analyzer, newsitem);         
    }
    
    
   
    
    
    public List<Comment> getComments() {     
    	List<Comment> matchingComments = new ArrayList<Comment>();    	
    	
        // TODO
        
        /*
        for (Comment comment :  ((Newsitem) resource).getComments()) {    		
            Comment highlightedComment = new KeywordHighlightingCommentDecorator(luceneQuery, analyzer, comment);
            final boolean commentHasMatches = !highlightedComment.getTitle().equals(comment.getTitle());
            if (commentHasMatches) {
                matchingComments.add(highlightedComment);
            }                    
		}
        */
    	return matchingComments;
	}
    

	public CommentFeed getCommentFeed() {
        return ((Newsitem) resource).getCommentFeed();
    }
        
    public void setCommentFeed(CommentFeed commentFeed) {
        ((Newsitem) resource).setCommentFeed(commentFeed);
    }
    public Website getPublisher() {
        return ((Newsitem) resource).getPublisher();
    }
    public void setPublisher(Website publisher) {
        ((Newsitem) resource).setPublisher(publisher);
    }

    public String getTwitterSubmitter() {
        return ((Newsitem) resource).getTwitterSubmitter();
    }

    public void setTwitterSubmitter(String submitter) {
        ((Newsitem) resource).setTwitterSubmitter(submitter);
    }
        
    public String getTwitterMessage() {
        return ((Newsitem) resource).getTwitterMessage();
    }

    public void setTwitterMessage(String message) {
        ((Newsitem) resource).setTwitterMessage(message);
    }
    
    
    
    
}
