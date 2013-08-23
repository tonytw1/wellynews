package nz.co.searchwellington.linkchecking;

import nz.co.searchwellington.feeds.CommentFeedReader;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NewsitemCommentReader implements LinkCheckerProcessor {

    private CommentFeedReader commentFeedReader;
    
   	@Autowired
	protected NewsitemCommentReader(CommentFeedReader commentFeedReader) {
		this.commentFeedReader = commentFeedReader;
	}
   	
	public void process(Resource checkResource, String pageContent) {
		if (checkResource.getType().equals("N") && ((Newsitem) checkResource).getCommentFeed() != null) {            
			commentFeedReader.loadCommentsFromCommentFeed(((Newsitem) checkResource).getCommentFeed());           
		}		
	}

}
