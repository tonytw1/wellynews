package nz.co.searchwellington.commentfeeds.guessers;

public class EyeOfTheFishCommentFeedGuesser implements CommentFeedGuesser {

    public boolean isValid(String url) {
        // TODO simpler expression for / example.
        return url != null && url.matches("http://.*eyeofthefish.org/.*?") && !url.matches("http://.*eyeofthefish.org/");
    }
    
    public String guessCommentFeedUrl(String url) {
        if (isValid(url)) {
            
            String[] splits = url.split("/");
            final String post = splits[splits.length-1];
            return "http://brownbag.wellington.gen.nz/feeds/eyeofthefish-comments" + url.substring(url.lastIndexOf("/"), url.length()) + post;
        }
        return null;
    }
      
}
