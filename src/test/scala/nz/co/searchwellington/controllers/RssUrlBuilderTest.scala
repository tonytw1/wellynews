package nz.co.searchwellington.controllers

import java.util.UUID

import org.junit.Assert.assertEquals
import nz.co.searchwellington.model.SiteInformation
import nz.co.searchwellington.model.Tag
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class RssUrlBuilderTest {
  @Mock val siteInformation: SiteInformation = null
  val tag = Tag(id = UUID.randomUUID().toString, description = Some("This is a tag about something..."))

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)
    Mockito.when(siteInformation.getAreaname).thenReturn("Somewhere")
  }

  @Test
  @throws[Exception]
  def rssDescriptionForTagShouldBeSetFromTagDescriptionTextIfAvailable(): Unit = {
    val rssUrlBuilder = new RssUrlBuilder(siteInformation)

    assertEquals("This is a tag about something...", rssUrlBuilder.getRssDescriptionForTag(tag))
  }
}
