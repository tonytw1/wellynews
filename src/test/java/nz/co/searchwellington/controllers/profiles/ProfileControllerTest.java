package nz.co.searchwellington.controllers.profiles;

import junit.framework.TestCase;

public class ProfileControllerTest extends TestCase {
	
	public void testname() throws Exception {		
		assertTrue(isProfilenameValid("tonytw1"));
	}
		
	private boolean isProfilenameValid(String profilename) {
		if (profilename.matches("[a-z|A-Z|0-9]+")) {
			return true;			  
		}		  
		return false;
	}
	
}
