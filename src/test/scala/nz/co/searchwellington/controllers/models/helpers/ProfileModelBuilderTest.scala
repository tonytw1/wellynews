package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.mock.web.MockHttpServletRequest

class ProfileModelBuilderTest extends ReasonableWaits {
  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val mongoRepository = mock(classOf[MongoRepository])

  val modelBuilder = new ProfileModelBuilder( contentRetrievalService, mongoRepository)

  @Test
  def usersProfilePathIsValid(): Unit = {
    val request = new MockHttpServletRequest()
    request.setRequestURI("/profiles/a-user")
    assertTrue(modelBuilder.isValid(request))
  }

}
