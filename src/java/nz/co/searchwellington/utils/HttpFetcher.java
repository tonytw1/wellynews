package nz.co.searchwellington.utils;

public interface HttpFetcher {

	public abstract HttpFetchResult httpFetch(String url);

}