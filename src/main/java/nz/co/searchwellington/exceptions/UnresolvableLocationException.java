package nz.co.searchwellington.exceptions;

public class UnresolvableLocationException extends RuntimeException {	// TODO needs to result in a 404
	
	public UnresolvableLocationException(String message) {
		super(message);
	}

	private static final long serialVersionUID = 1L;

}
