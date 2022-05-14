package nz.co.searchwellington.commentfeeds.detectors;

import java.net.URL;

public class TextureCommentFeedDetector implements CommentFeedDetector {

    public boolean isValid(URL url) {
        return url != null && 
        (	url.toExternalForm().startsWith("http://texture.co.nz/blogs/features/commentrss.aspx?PostID=")
        	|| url.toExternalForm().startsWith("http://texture.co.nz/blogs/news/commentrss.aspx?PostID="));
    }

}
