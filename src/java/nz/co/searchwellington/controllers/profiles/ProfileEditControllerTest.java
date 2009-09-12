package nz.co.searchwellington.controllers.profiles;

import junit.framework.TestCase;

public class ProfileEditControllerTest extends TestCase {

	
	public void testname() throws Exception {
		
		
		System.out.println(isProfilenameValid("tonytw1"));
	}
	
	
	private boolean isProfilenameValid(String profilename) {
		  if (profilename.matches("[a-z|A-Z|0-9]+")) {
			 return true;			  
		  }		  
		  return false;
	  }	
}
