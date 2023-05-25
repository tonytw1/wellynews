package nz.co.searchwellington.urls

import nz.co.searchwellington.model.{SiteInformation, Tag}
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

import java.util.UUID

class RssUrlBuilderTest {

  private val siteInformation = new SiteInformation(areaname = "Somewhere", url = "https://wellynews.local")
  private val rssUrlBuilder = new RssUrlBuilder(siteInformation)

  @Test
  def rssDescriptionForTagShouldBeSetFromTagDescriptionTextIfAvailable(): Unit = {
    val tag = Tag(id = UUID.randomUUID().toString, description = Some("This is a tag about something..."))

    assertEquals("This is a tag about something...", rssUrlBuilder.getRssDescriptionForTag(tag))
  }

  @Test
  def tagRssUrlIsBasedOnTagName(): Unit = {
    val tag = Tag(id = UUID.randomUUID().toString, description = Some("This is a tag about something..."), name = "atag")

    assertEquals("https://wellynews.local/atag/rss", rssUrlBuilder.getRssUrlForTag(tag))
  }

}
