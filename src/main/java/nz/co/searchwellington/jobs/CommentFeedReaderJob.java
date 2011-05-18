package nz.co.searchwellington.jobs;

import java.io.IOException;

import nz.co.searchwellington.feeds.CommentFeedReader;

import org.apache.log4j.Logger;

import com.sun.syndication.io.FeedException;

public class CommentFeedReaderJob {

    Logger log = Logger.getLogger(CommentFeedReaderJob.class);

    private CommentFeedReader commentReader;

    
    public CommentFeedReaderJob(CommentFeedReader commentReader) {
        this.commentReader = commentReader;
    }


    public void runComments() throws FeedException, IOException {
        log.info("Running Comment Reader.");
        commentReader.loadComments();     
    }



}
