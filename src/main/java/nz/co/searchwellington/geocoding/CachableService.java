package nz.co.searchwellington.geocoding;

public interface CachableService<T, U> {

	public U callService(T parameter);
	
	public String getCacheKeyFor(T parameter);
	
	public int getTTL();
	
}
