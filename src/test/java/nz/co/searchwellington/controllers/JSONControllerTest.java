package nz.co.searchwellington.controllers;

import junit.framework.TestCase;

public class JSONControllerTest extends TestCase {
	
	JSONController controller;
	
	@Override
	protected void setUp() throws Exception {
		controller = new JSONController(null);
	}
	
	public void testShouldAcceptValidCallbackName() throws Exception {
		assertTrue(controller.isValidCallbackName("_callBack123"));
	}
	
	public void testShouldRejectInvalidCallbackName() throws Exception {
		JSONController controller = new JSONController(null);
		assertFalse(controller.isValidCallbackName("callback()"));
	}

}
