package nz.co.searchwellington.jobs;

import java.io.IOException;

import nz.co.searchwellington.feeds.CommentFeedReader;
import nz.co.searchwellington.feeds.FeedReader;

import org.apache.log4j.Logger;

import com.sun.syndication.io.FeedException;

public class FeedReaderJob {

    Logger log = Logger.getLogger(FeedReaderJob.class);

    private FeedReader feedReader;
    private CommentFeedReader commentReader;

 

    public FeedReaderJob(FeedReader feedReader, CommentFeedReader commentReader) {
        this.feedReader = feedReader;
        this.commentReader = commentReader;
    }


    public void runFeeds() throws FeedException, IOException {
        log.info("Running FeedReader.");
        feedReader.acceptFeeditems();
    }
    

    public void runComments() throws FeedException, IOException {
        log.info("Running Comment Reader.");
        commentReader.loadComments();     
    }



}
