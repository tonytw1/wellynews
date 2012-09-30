package nz.co.searchwellington.jobs;

import java.io.IOException;

import nz.co.searchwellington.feeds.CommentFeedReader;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sun.syndication.io.FeedException;

@Component
public class CommentFeedReaderJob {

	private static Logger log = Logger.getLogger(CommentFeedReaderJob.class);

    private CommentFeedReader commentReader;

    @Autowired
    public CommentFeedReaderJob(CommentFeedReader commentReader) {
        this.commentReader = commentReader;
    }
    
    public void runComments() throws FeedException, IOException {
        log.info("Running Comment Reader.");
        commentReader.loadComments();     
    }
    
}
