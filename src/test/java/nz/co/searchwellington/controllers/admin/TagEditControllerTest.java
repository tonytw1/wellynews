package nz.co.searchwellington.controllers.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.controllers.SubmissionProcessingService;
import nz.co.searchwellington.controllers.UrlStack;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.modification.TagModificationService;
import nz.co.searchwellington.repositories.TagDAO;
import nz.co.searchwellington.widgets.TagWidgetFactory;

import org.apache.struts.mock.MockHttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

public class TagEditControllerTest {

	private static final String NEW_TAG_DISPLAY_NAME = "A new tag";
	
	@Mock AdminRequestFilter requestFilter;
	@Mock TagWidgetFactory tagWidgetFactory;
	@Mock UrlStack urlStack;
	@Mock TagDAO tagDAO;
	@Mock TagModificationService tagModifcationService;
	@Mock LoggedInUserFilter loggedInUserFilter;
	@Mock EditPermissionService editPermissionService;
	@Mock SubmissionProcessingService submissionProcessingService;

	@Mock Tag newTag;
	@Mock Tag existingTag;
	
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private TagEditController controller;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);				
		request = new MockHttpServletRequest();		
		controller = new TagEditController(requestFilter, 
				tagWidgetFactory, urlStack, tagDAO, tagModifcationService, loggedInUserFilter, editPermissionService,
				submissionProcessingService);
	}

	@Test
	public void shouldCreateAndSaveNewTagBasedOnDisplayName() throws Exception {				
		Mockito.when(tagDAO.createNewTag("a-new-tag", NEW_TAG_DISPLAY_NAME)).thenReturn(newTag);	
		request.setParameter("displayName", NEW_TAG_DISPLAY_NAME);
		
		ModelAndView mv = controller.add(request, response);
				
		Tag addedTag = (Tag) mv.getModel().get("tag");
		assertEquals(newTag, addedTag);
		Mockito.verify(tagDAO).saveTag(newTag);
	}
	
	@Test
	public void shouldRejectNewTagIfUrlWordsClauseWithAnExistingTag() throws Exception {
		Mockito.when(tagDAO.createNewTag("an-existing-tag", "An existing tag")).thenReturn(newTag);
		Mockito.when(tagDAO.loadTagByName("an-existing-tag")).thenReturn(existingTag);
		request.setParameter("displayName", "An existing tag");

		ModelAndView mv = controller.add(request, response);
		
		assertNull(mv.getModel().get("tag"));
	}
	
}
