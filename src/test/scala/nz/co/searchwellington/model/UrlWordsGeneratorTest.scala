package nz.co.searchwellington.model

import org.joda.time.{DateTime, DateTimeZone}
import org.junit.Assert.assertEquals
import org.junit.Test
import uk.co.eelpieconsulting.common.dates.DateFormatter

class UrlWordsGeneratorTest {

  private val april2010 = new DateTime(2010, 4, 2, 12, 0).withZone(DateTimeZone.UTC).toDate

  private val urlWordsGenerator = new UrlWordsGenerator(new DateFormatter(DateTimeZone.UTC))

  @Test
  def shouldProduceCorrectUrlBasedOnPublisherDateAndHeadline(): Unit = {
    val publisher = Website(title = "Island Bay school")
    val newsitemWithPublisher = Newsitem(title = "Something happened at the school", date = Some(april2010), publisher = Some(publisher._id))

    assertEquals(Some("/island-bay-school/2010/apr/2/something-happened-at-the-school"), urlWordsGenerator.makeUrlWordsFor(newsitemWithPublisher, Some(publisher)))
  }

  @Test
  def urlWordshouldBeDateAndHeadlineIfPublisherIsNotSet(): Unit = {
    val newsitemWithNoPublisher = Newsitem(title = "Something-happening", date = Some(april2010))
    assertEquals(Some("/2010/apr/2/something-happening"), urlWordsGenerator.makeUrlWordsFor(newsitemWithNoPublisher))
  }

}
