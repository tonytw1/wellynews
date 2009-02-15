package nz.co.searchwellington.utils;


public interface RedirectingUrlResolver {

    public abstract boolean isValid(String url);

    public String resolveUrl(String url);

}
