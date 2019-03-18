package nz.co.searchwellington.model

import java.util.UUID

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
    val pubdate = new DateTime(2010, 4, 2, 0, 0, 0, 0, DateTimeZone.UTC).toDate
    newsitem = FrontendNewsitem(id = UUID.randomUUID().toString, name = "Something happening", date = pubdate)
    this.urlWordsGenerator = new UrlWordsGenerator
  }

  /*
  @Test
  @throws[Exception]
  def shouldProduceCorrectUrlBasedOnPublisherDateAndHeadline {
    val publisher = new Website(title = Some("Island Bay school"))
    newsitem.setPublisherName("Island Bay school")
    assertEquals("/island-bay-school/2010/apr/2/some-thing-happening", urlWordsGenerator.makeUrlForNewsitem(newsitem))
  }
  */

  /*
  @Test
  @throws[Exception]
  def urlWordshouldBeDateAndHeadlineIfPublisherIsNotSet {
    assertEquals("/2010/apr/2/some-thing-happening", urlWordsGenerator.makeUrlForNewsitem(newsitem))
  }
  */

  @Test
  @throws[Exception]
  def shouldBeNullSafe {
    assertEquals(null, urlWordsGenerator.makeUrlWordsFromName(null))
  }

}
