package nz.co.searchwellington.jobs;

import java.io.IOException;

import nz.co.searchwellington.feeds.CommentFeedReader;

import org.apache.log4j.Logger;

import com.sun.syndication.io.FeedException;

public class FeedReaderJob {

    Logger log = Logger.getLogger(FeedReaderJob.class);

    private CommentFeedReader commentReader;

 

    public FeedReaderJob(CommentFeedReader commentReader) {
        this.commentReader = commentReader;
    }


    public void runComments() throws FeedException, IOException {
        log.info("Running Comment Reader.");
        commentReader.loadComments();     
    }



}
