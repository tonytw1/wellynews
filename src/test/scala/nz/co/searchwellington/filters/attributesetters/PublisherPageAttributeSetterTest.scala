package nz.co.searchwellington.filters.attributesetters

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Website
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.{mock, when}
import org.springframework.mock.web.MockHttpServletRequest

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class PublisherPageAttributeSetterTest extends ReasonableWaits {
  private val mongoRepository: MongoRepository = mock(classOf[MongoRepository])
  private val publisher = Website(id = UUID.randomUUID().toString, title = "Wellington City Council")
  private val request = new MockHttpServletRequest

  private val publisherPageAttributeSetter = new PublisherPageAttributeSetter(mongoRepository)

  @BeforeEach
  def setup(): Unit = {
    when(mongoRepository.getWebsiteByUrlwords("wellington-city-council")).thenReturn(Future.successful(Some(publisher)))
  }

  @Test
  def shouldSetPublisherAttributeForPublisherPath(): Unit = {
    request.setRequestURI("/wellington-city-council")

    val attributes = Await.result(publisherPageAttributeSetter.setAttributes(request), TenSeconds)

    assertEquals(publisher, attributes("publisher"))
  }


  @Test
  def shouldSetPublisherAttributeForPublisherArchivePath(): Unit = {
    request.setRequestURI("/wellington-city-council/2020-mar")

    val attributes = Await.result(publisherPageAttributeSetter.setAttributes(request), TenSeconds)

    assertEquals(publisher, attributes("publisher"))
  }

  @Test
  def shouldSetPublisherAttributeForPublisherRssPath(): Unit = {
    request.setRequestURI("/wellington-city-council/rss")

    val attributes = Await.result(publisherPageAttributeSetter.setAttributes(request), TenSeconds)

    assertEquals(publisher, attributes("publisher"))
  }

}
