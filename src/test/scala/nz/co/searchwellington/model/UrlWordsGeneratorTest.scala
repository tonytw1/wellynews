package nz.co.searchwellington.model

import org.junit.Assert.assertEquals
import nz.co.searchwellington.model.frontend.FrontendNewsitem
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Before
import org.junit.Test

class UrlWordsGeneratorTest {
  private var urlWordsGenerator: UrlWordsGenerator = null
  private var newsitem: FrontendNewsitem = null

  @Before def setup {
    newsitem = new FrontendNewsitem
    newsitem.setName("Some thing happening")
    val pubdate = new DateTime(2010, 4, 2, 0, 0, 0, 0, DateTimeZone.UTC)
    newsitem.setDate(pubdate.toDate)
    this.urlWordsGenerator = new UrlWordsGenerator
  }

  @Test
  @throws[Exception]
  def shouldProduceCorrectUrlBasedOnPublisherDateAndHeadline {
    val publisher = new Website(title = Some("Island Bay school"))
    newsitem.setPublisherName("Island Bay school")
    assertEquals("/island-bay-school/2010/apr/2/some-thing-happening", urlWordsGenerator.makeUrlForNewsitem(newsitem))
  }

  @Test
  @throws[Exception]
  def urlWordshouldBeDateAndHeadlineIfPublisherIsNotSet {
    assertEquals("/2010/apr/2/some-thing-happening", urlWordsGenerator.makeUrlForNewsitem(newsitem))
  }

  @Test
  @throws[Exception]
  def shouldBeNullSafe {
    assertEquals(null, urlWordsGenerator.makeUrlWordsFromName(null))
  }

}
