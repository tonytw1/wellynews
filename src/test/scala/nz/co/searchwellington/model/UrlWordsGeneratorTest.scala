package nz.co.searchwellington.model

import org.joda.time.{DateTime, DateTimeZone}
import org.junit.Assert.assertEquals
import org.junit.Test

class UrlWordsGeneratorTest {

  private val april2010 = new DateTime(2010, 4, 2, 12, 0).withZone(DateTimeZone.UTC).toDate

  private val urlWordsGenerator = new UrlWordsGenerator

  @Test
  def shouldProduceCorrectUrlBasedOnPublisherDateAndHeadline {
    val publisher = Website(title = Some("Island Bay school"))
    val newsitemWithPublisher = Newsitem(title = Some("Something happened at the school"), date = Some(april2010), publisher = Some(publisher._id))

    assertEquals(Some("/island-bay-school/2010/apr/2/something-happened"), urlWordsGenerator.makeUrlForNewsitem(newsitemWithPublisher))
  }

  @Test
  def urlWordshouldBeDateAndHeadlineIfPublisherIsNotSet {
    val newsitemWithNoPublisher = Newsitem(title = Some("Something-happening"), date = Some(april2010))
    assertEquals(Some("/2010/apr/2/something-happening"), urlWordsGenerator.makeUrlForNewsitem(newsitemWithNoPublisher))
  }

  @Test
  def shouldBeNullSafe {
    assertEquals(null, urlWordsGenerator.makeUrlWordsFromName(null))
  }

}
