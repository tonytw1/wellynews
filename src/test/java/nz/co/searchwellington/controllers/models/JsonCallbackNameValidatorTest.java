package nz.co.searchwellington.controllers.models;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class JsonCallbackNameValidatorTest {

	@Test
	public void validCallNamesCanHaveLettersNumbersAndUnderScoresInThem() throws Exception {
		JsonCallbackNameValidator validator = new JsonCallbackNameValidator();
		assertTrue(validator.isValidCallbackName("Abc_de123"));
	}
	
	@Test
	public void spacesAreNotValidInCallbackNames() throws Exception {
		JsonCallbackNameValidator validator = new JsonCallbackNameValidator();
		assertFalse(validator.isValidCallbackName("Abc de123"));
	}
	
}
