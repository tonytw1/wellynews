package nz.co.searchwellington.controllers;

import junit.framework.TestCase;

public class BaseTagEditingControllerTest extends TestCase {
	
	public void testShouldDetectInvalidTagnames() throws Exception {
		
		BaseTagEditingController controller = new BaseTagEditingController();
				
		assertTrue(controller.isValidTagName("tag"));
		assertTrue(controller.isValidTagName("tag123"));
		assertTrue(controller.isValidTagName("123"));
		
		assertFalse(controller.isValidTagName("tag "));
		assertFalse(controller.isValidTagName(" tag "));		
		assertFalse(controller.isValidTagName(" tag"));		
		assertFalse(controller.isValidTagName("tag name"));
		assertFalse(controller.isValidTagName(""));
		assertFalse(controller.isValidTagName(null));
	}

}
