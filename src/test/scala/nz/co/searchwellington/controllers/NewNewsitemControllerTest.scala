package nz.co.searchwellington.controllers

import nz.co.searchwellington.forms.NewNewsitem
import nz.co.searchwellington.model.{Newsitem, User, Website}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.{UrlBuilder, UrlCleaner}
import org.apache.struts.mock.MockHttpServletRequest
import org.joda.time.DateTime
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.{mock, verify, when}
import org.mockito.{ArgumentCaptor, Matchers}
import org.springframework.validation.BindingResult
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.{ExecutionContext, Future}

class NewNewsitemControllerTest {

  private val contentUpdateService = mock(classOf[ContentUpdateService])
  private val mongoRepository = mock(classOf[MongoRepository])
  private val urlBuilder = mock(classOf[UrlBuilder])
  private val anonUserService = mock(classOf[AnonUserService])
  private val urlCleaner = mock(classOf[UrlCleaner])

  val controller = new NewNewsitemController(contentUpdateService, mongoRepository, urlBuilder, anonUserService, urlCleaner)

  @Test
  def canSubmitNewsitems(): Unit = {
    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    val newNewsitemSubmission = new NewNewsitem()
    newNewsitemSubmission.setTitle("A newsitem")
    newNewsitemSubmission.setUrl("https://localhost/a-newsitem")
    newNewsitemSubmission.setDate("20200122")
    newNewsitemSubmission.setPublisher("A publisher")
    newNewsitemSubmission.setDescription("Something interesting")

    val publisher = Website(_id = BSONObjectID.generate(), title = Some("A publisher"))
    when(mongoRepository.getWebsiteByName("A publisher")).thenReturn(Future.successful(Some(publisher)))
    when(urlCleaner.cleanSubmittedItemUrl("https://localhost/a-newsitem")).thenReturn("https://localhost/a-newsitem")
    val bindingResultWithNoErrors = mock(classOf[BindingResult])
    val createdNewsitem = ArgumentCaptor.forClass(classOf[Newsitem])

    val request = new MockHttpServletRequest
    request.getSession().setAttribute("user", User())
    controller.submit(newNewsitemSubmission, bindingResultWithNoErrors, request)

    verify(contentUpdateService).create(createdNewsitem.capture)(Matchers.eq(ec))
    assertEquals(Some("A newsitem"), createdNewsitem.getValue.title)
    assertEquals("https://localhost/a-newsitem", createdNewsitem.getValue.page)
    assertEquals(Some(new DateTime(2020, 1, 22, 0, 0).toDate), createdNewsitem.getValue.date)
    assertEquals(publisher._id, createdNewsitem.getValue.publisher.get)
    assertEquals(Some("Something interesting"), createdNewsitem.getValue.description)
   }

}
