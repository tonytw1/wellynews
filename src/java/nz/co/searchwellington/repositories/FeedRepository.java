package nz.co.searchwellington.repositories;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Resource;

import com.sun.syndication.io.FeedException;

public interface FeedRepository {
    
    public List <Resource> getFeedNewsitems(Feed feed) throws IllegalArgumentException, FeedException, IOException;
    public Date getLatestPublicationDate(Feed feed) throws IllegalArgumentException, IOException;

}
