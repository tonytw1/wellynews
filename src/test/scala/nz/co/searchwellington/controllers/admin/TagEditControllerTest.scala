package nz.co.searchwellington.controllers.admin

import nz.co.searchwellington.model.{Tag, UrlWordsGenerator}
import nz.co.searchwellington.repositories.TagDAO
import org.junit.Assert.{assertEquals, assertNull}
import org.junit.{Before, Test}
import org.mockito.Mockito.{verify, when}
import org.mockito.{Mock, MockitoAnnotations}
import org.springframework.mock.web.MockHttpServletRequest

class TagEditControllerTest {
  @Mock val requestFilter = null
  @Mock val tagWidgetFactory = null
  @Mock val urlStack = null
  @Mock val tagDAO: TagDAO = null
  @Mock val tagModifcationService = null
  @Mock val loggedInUserFilter = null
  @Mock val editPermissionService = null
  @Mock val submissionProcessingService = null
  @Mock val commonModelObjectsService = null
  @Mock val urlWordsGenerator: UrlWordsGenerator = null
  @Mock val newTag = null
  @Mock val existingTag = null

  private var request: MockHttpServletRequest = null
  private val response = null
  private var controller: TagEditController = null

  private val NEW_TAG_DISPLAY_NAME = "A new tag"

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)
    request = new MockHttpServletRequest
    controller = new TagEditController(requestFilter, tagWidgetFactory, urlStack, tagDAO, tagModifcationService, loggedInUserFilter, editPermissionService, submissionProcessingService, commonModelObjectsService, urlWordsGenerator)
  }

  @Test
  @throws[Exception]
  def shouldCreateAndSaveNewTagBasedOnDisplayName(): Unit = {
    when(urlWordsGenerator.makeUrlWordsFromName(NEW_TAG_DISPLAY_NAME)).thenReturn("a-new-tag")
    when(tagDAO.createNewTag("a-new-tag", NEW_TAG_DISPLAY_NAME)).thenReturn(newTag)
    request.setParameter("displayName", NEW_TAG_DISPLAY_NAME)

    val mv = controller.add(request, response)

    val addedTag = mv.getModel.get("tag").asInstanceOf[Tag]
    assertEquals(newTag, addedTag)
    verify(tagDAO).saveTag(newTag)
  }

  @Test
  @throws[Exception]
  def shouldRejectNewTagIfUrlWordsClauseWithAnExistingTag(): Unit = {
    when(tagDAO.createNewTag("an-existing-tag", "An existing tag")).thenReturn(newTag)
    when(tagDAO.loadTagByName("an-existing-tag")).thenReturn(existingTag)
    request.setParameter("displayName", "An existing tag")

    val mv = controller.add(request, response)

    assertNull(mv.getModel.get("tag"))
  }

}