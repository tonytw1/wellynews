package nz.co.searchwellington.controllers.admin

import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.model.frontend.FrontendWebsite
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{User, Website}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlParser
import org.junit.Assert.{assertEquals, assertNotNull}
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import org.springframework.web.servlet.ModelAndView
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.Future
import scala.jdk.CollectionConverters._
import scala.concurrent.ExecutionContext.Implicits.global

class GatherControllerTest {

  private val mongoRepository: MongoRepository = mock(classOf[MongoRepository])
  private val contentUpdateService = mock(classOf[ContentUpdateService])
  private val urlParser = new UrlParser
  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val frontendResourceMapper = mock(classOf[FrontendResourceMapper])
  private val loggedInUserFilter = mock(classOf[LoggedInUserFilter])

  @Test
  def promptScreenShouldListsMatchesForPublisher(): Unit = {
    val controller = new GatherController(mongoRepository, contentUpdateService, urlParser, contentRetrievalService, frontendResourceMapper, loggedInUserFilter)
    val publisher = Website(id = BSONObjectID.generate().stringify, page = "https://www.foo.com/")
    val frontendPublisher = FrontendWebsite(id = publisher.id)
    val adminUser = User(admin = true)
    when(loggedInUserFilter.getLoggedInUser).thenReturn(Some(adminUser))
    when(mongoRepository.getResourceById(ArgumentMatchers.eq(publisher.id))(any)).thenReturn(Future.successful(Some(publisher)))
    when(frontendResourceMapper.createFrontendResourceFrom(ArgumentMatchers.eq(publisher), any)(any)).thenReturn(Future.successful(frontendPublisher))
    val gatheredResources = Seq.empty
    when(contentRetrievalService.getPublishedResourcesMatchingHostname(publisher, "www.foo.com", Some(adminUser))).thenReturn(Future.successful(gatheredResources))

    val mv: ModelAndView = controller.prompt(publisher.id)

    assertNotNull(mv)
    assertEquals(frontendPublisher, mv.getModel.get("publisher"))
    assertEquals(gatheredResources.asJava, mv.getModel.get("gathered"))
  }

}
