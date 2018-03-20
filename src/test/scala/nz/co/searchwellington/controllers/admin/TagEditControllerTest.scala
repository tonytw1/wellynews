package nz.co.searchwellington.controllers.admin

import nz.co.searchwellington.controllers.{CommonModelObjectsService, LoggedInUserFilter, SubmissionProcessingService, UrlStack}
import nz.co.searchwellington.filters.AdminRequestFilter
import nz.co.searchwellington.model.{Tag, UrlWordsGenerator}
import nz.co.searchwellington.modification.TagModificationService
import nz.co.searchwellington.permissions.EditPermissionService
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.widgets.TagsWidgetFactory
import org.junit.Assert.{assertEquals, assertNull}
import org.junit.Test
import org.mockito.Mockito.{mock, verify, when}
import org.springframework.mock.web.MockHttpServletRequest

class TagEditControllerTest {
   val requestFilter = mock(classOf[AdminRequestFilter])
   val tagWidgetFactory = mock(classOf[TagsWidgetFactory])
   val urlStack = mock(classOf[UrlStack])
   val tagDAO = mock(classOf[TagDAO])
   val tagModifcationService = mock(classOf[TagModificationService])
   val loggedInUserFilter = mock(classOf[LoggedInUserFilter])
   val editPermissionService = mock(classOf[EditPermissionService])
   val submissionProcessingService = mock(classOf[SubmissionProcessingService])
   val commonModelObjectsService = mock(classOf[CommonModelObjectsService])
   val urlWordsGenerator = mock(classOf[UrlWordsGenerator])

   val newTag = Tag(name = "A new tag")
   val existingTag = Tag(name = "An existing tag")

  private val request = new MockHttpServletRequest
  private val response = null

  private val controller = new TagEditController(requestFilter, tagWidgetFactory, urlStack, tagDAO, tagModifcationService, loggedInUserFilter, editPermissionService, submissionProcessingService, commonModelObjectsService, urlWordsGenerator)

  private val NEW_TAG_DISPLAY_NAME = "A new tag"

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
    when(tagDAO.loadTagByName("an-existing-tag")).thenReturn(Some(existingTag))
    request.setParameter("displayName", "An existing tag")

    val mv = controller.add(request, response)

    assertNull(mv.getModel.get("tag"))
  }

}