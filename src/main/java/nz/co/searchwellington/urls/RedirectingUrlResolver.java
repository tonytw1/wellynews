package nz.co.searchwellington.urls;

public interface RedirectingUrlResolver {

    public abstract boolean isValid(String url);

    public String resolveUrl(String url);

}
