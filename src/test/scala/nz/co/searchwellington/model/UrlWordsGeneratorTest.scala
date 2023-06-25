package nz.co.searchwellington.model

import org.joda.time.{DateTime, DateTimeZone}
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.co.eelpieconsulting.common.dates.DateFormatter

class UrlWordsGeneratorTest {

  private val april2010 = new DateTime(2010, 4, 2, 12, 0).withZone(DateTimeZone.UTC).toDate

  private val urlWordsGenerator = new UrlWordsGenerator(new DateFormatter(DateTimeZone.UTC))

  @Test
  def shouldProduceSeoStyleUrlWordsForPublisher(): Unit = {
    val publisher = Website(title = "Island Bay school")

    val urlWords = urlWordsGenerator.makeUrlWordsFor(publisher, None)

    assertEquals("island-bay-school", urlWords)
  }

  @Test
  def shouldProduceCorrectUrlBasedOnPublisherDateAndHeadline(): Unit = {
    val publisher = Website(title = "Island Bay school")
    val newsitemWithPublisher = Newsitem(title = "Something happened at the school", date = april2010, publisher = Some(publisher._id))

    val urlWords = urlWordsGenerator.makeUrlWordsFor(newsitemWithPublisher, Some(publisher))

    assertEquals("island-bay-school/2010/apr/2/something-happened-at-the-school", urlWords)  // TODO should URLS words be slash prefixed?
  }

  @Test
  def urlWordshouldBeDateAndHeadlineIfPublisherIsNotSet(): Unit = {
    val newsitemWithNoPublisher = Newsitem(title = "Something-happening", date = april2010)

    val urlWords = urlWordsGenerator.makeUrlWordsFor(newsitemWithNoPublisher)

    assertEquals("2010/apr/2/something-happening", urlWords)
  }

}
