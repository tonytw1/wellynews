package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.model._
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.junit.Assert.{assertFalse, assertTrue}
import org.junit.Test
import org.mockito.Mockito.mock
import org.springframework.mock.web.MockHttpServletRequest
import uk.co.eelpieconsulting.common.dates.DateFormatter

class TagMonthModelBuilderTest extends ReasonableWaits with ContentFields {

  private val siteInformation = new SiteInformation("", "", "", "", "")
  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val rssUrlBuilder = new RssUrlBuilder(siteInformation)

  private val parentTag = Tag(display_name = "Parent")
  private val tag = Tag(parent = Some(parentTag._id), display_name = "Penguins", name = "penguins")

  private val modelBuilder = new TagMonthModelBuilder(contentRetrievalService, new DateFormatter("Europe/London"), rssUrlBuilder)

  @Test
  def validForTagRequestWithMonthSuffix(): Unit = {
    val request = new MockHttpServletRequest()
    request.setAttribute("tags", Seq(tag))
    request.setRequestURI("/" + tag.name + "/2022-apr")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def validForJsonSuffix(): Unit = {
    val request = new MockHttpServletRequest()
    request.setAttribute("tags", Seq(tag))
    request.setRequestURI("/" + tag.name + "/2022-apr/json")
    assertTrue(modelBuilder.isValid(request))
  }

  @Test
  def notValidForTagRequestWithInvalidMonthSuffix(): Unit = {
    val request = new MockHttpServletRequest()
    request.setAttribute("tags", Seq(tag))
    request.setRequestURI("/" + tag.name + "/2022-meh")
    assertFalse(modelBuilder.isValid(request))
  }

}
