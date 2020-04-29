package nz.co.searchwellington.controllers

import nz.co.searchwellington.forms.NewNewsitem
import nz.co.searchwellington.model.{Newsitem, User, Website}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import org.joda.time.DateTime
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.{mock, verify, when}
import org.mockito.{ArgumentCaptor, Matchers}
import org.springframework.validation.BindingResult
import reactivemongo.bson.BSONObjectID

import scala.concurrent.Future

class NewNewsitemControllerTest {

  private val contentUpdateService = mock(classOf[ContentUpdateService])
  private val loggedInUserFilter = mock(classOf[LoggedInUserFilter])
  private val mongoRepository = mock(classOf[MongoRepository])
  private val urlBuilder = mock(classOf[UrlBuilder])

  val controller = new NewNewsitemController(contentUpdateService, loggedInUserFilter, mongoRepository, urlBuilder)

  @Test
  def canSubmitNewsitems(): Unit = {
    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

    val newNewsitemSubmission = new NewNewsitem()
    newNewsitemSubmission.setTitle("A newsitem")
    newNewsitemSubmission.setUrl("https://localhost/a-newsitem")
    newNewsitemSubmission.setDate("20200122")
    newNewsitemSubmission.setPublisher("A publisher")
    newNewsitemSubmission.setDescription("Something interesting")

    val publisher = Website(_id = BSONObjectID.generate(), title = Some("A publisher"))
    when(loggedInUserFilter.getLoggedInUser).thenReturn(Some(User()))
    when(mongoRepository.getWebsiteByName("A publisher")).thenReturn(Future.successful(Some(publisher)))

    val bindingResultWithNoErrors = mock(classOf[BindingResult])
    val createdNewsitem = ArgumentCaptor.forClass(classOf[Newsitem])

    controller.submit(newNewsitemSubmission, bindingResultWithNoErrors)

    verify(contentUpdateService).create(createdNewsitem.capture)(Matchers.eq(ec))
    assertEquals(Some("A newsitem"), createdNewsitem.getValue.title)
    assertEquals(Some("https://localhost/a-newsitem"), createdNewsitem.getValue.page)
    assertEquals(Some(new DateTime(2020, 1, 22, 0, 0).toDate), createdNewsitem.getValue.date)
    assertEquals(publisher._id, createdNewsitem.getValue.publisher.get)
    assertEquals(Some("Something interesting"), createdNewsitem.getValue.description)
   }

}
