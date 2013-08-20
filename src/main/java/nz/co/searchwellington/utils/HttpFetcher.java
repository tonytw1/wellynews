package nz.co.searchwellington.utils;

@Deprecated // TODO move to common http fetcher
public interface HttpFetcher {

	public abstract HttpFetchResult httpFetch(String url);

}