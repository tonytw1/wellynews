package nz.co.searchwellington.controllers.api;

public class ApiKeyAuthenticator {

	public boolean isAuthentic(String key) {
		return "tony".equals(key);
	}

}
