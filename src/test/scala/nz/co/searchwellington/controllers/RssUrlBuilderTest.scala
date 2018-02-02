package nz.co.searchwellington.controllers

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
  @Mock val tag: Tag = null

  @Before def setup(): Unit = {
    MockitoAnnotations.initMocks(this)
    Mockito.when(siteInformation.getAreaname).thenReturn("Somewhere")
  }

  @Test
  @throws[Exception]
  def rssDescriptionForTagShouldBeSetFromTagDescriptionTextIfAvailable(): Unit = {
    Mockito.when(tag.getDescription).thenReturn(Some("This is a tag about something..."))
    val rssUrlBuilder = new RssUrlBuilder(siteInformation)
    assertEquals(tag.getDescription, rssUrlBuilder.getRssDescriptionForTag(tag))
  }
}
