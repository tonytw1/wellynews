package nz.co.searchwellington.commentfeeds.detectors;

public class TextureCommentFeedDetector implements CommentFeedDetector {

    public boolean isValid(String url) {
        return url != null && 
        (	url.startsWith("http://texture.co.nz/blogs/features/commentrss.aspx?PostID=")
        	|| url.startsWith("http://texture.co.nz/blogs/news/commentrss.aspx?PostID="));
    }

}
