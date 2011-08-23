package nz.co.searchwellington.controllers.models;

public class JsonCallbackNameValidator {
	
	private static final String VALID_CALLBACK_NAME_REGEX = "[a-z|A-Z|0-9|_]+";
			
	public boolean isValidCallbackName(String callback) {
		return callback.matches(VALID_CALLBACK_NAME_REGEX);
	}
	

}
