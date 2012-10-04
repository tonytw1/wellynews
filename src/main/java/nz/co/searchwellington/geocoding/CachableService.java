package nz.co.searchwellington.geocoding;

public interface CachableService<T, U> {

	public U callService(T parameter);
	
}
