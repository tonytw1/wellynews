package nz.co.searchwellington.controllers;

import junit.framework.TestCase;
import nz.co.searchwellington.model.Tag;

import org.springframework.web.servlet.ModelAndView;


public class TagControllerTests extends TestCase {
	
	public void testShouldCorrectlyEscapeTagEditUrl() throws Exception {
		ModelAndView mv = new ModelAndView();
		TagController controller = new TagController(null, null, null, null, null, null, null, null);
		
		Tag tagWithSimpleName = new Tag(1, "test", "test", null, null, 0);
		controller.populateTagEditUrl(mv, tagWithSimpleName);
		assertNotNull(mv.getModel().get("editurl"));		
		assertEquals("/edit/tag/test", mv.getModel().get("editurl"));

		Tag tagWithSpecialCharactersInName = new Tag(1, "test tag", "test tag", null, null, 0);
		controller.populateTagEditUrl(mv, tagWithSpecialCharactersInName);
		assertEquals("/edit/tag/test+tag", mv.getModel().get("editurl"));
	}
	

}
