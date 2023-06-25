package nz.co.searchwellington.controllers

import nz.co.searchwellington.forms.NewNewsitem
import nz.co.searchwellington.geocoding.osm.GeoCodeService
import nz.co.searchwellington.model.{Newsitem, Resource, User, Website}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.{ContentRetrievalService, TagDAO}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.{UrlBuilder, UrlCleaner}
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, verify, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.validation.BindingResult
import reactivemongo.api.bson.BSONObjectID

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

class NewNewsitemControllerTest {

  private val contentUpdateService = mock(classOf[ContentUpdateService])
  private val mongoRepository = mock(classOf[MongoRepository])
  private val urlBuilder = mock(classOf[UrlBuilder])
  private val anonUserService = mock(classOf[AnonUserService])
  private val urlCleaner = mock(classOf[UrlCleaner])
  private val geoCodeService = mock(classOf[GeoCodeService])
  private val contentRetrievalService = mock(classOf[ContentRetrievalService])
  private val tagDAO = mock(classOf[TagDAO])
  private val loggedInUserFilter = mock(classOf[LoggedInUserFilter])

  val controller = new NewNewsitemController(contentUpdateService, mongoRepository, urlBuilder, anonUserService,
    urlCleaner, geoCodeService, contentRetrievalService, tagDAO, loggedInUserFilter)

  @Test
  def shouldSubmitNewsitems(): Unit = {
    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    val newNewsitemSubmission = new NewNewsitem()
    newNewsitemSubmission.setTitle("A newsitem")
    newNewsitemSubmission.setUrl("localhost/a-newsitem    ")
    newNewsitemSubmission.setDate("20200122")
    newNewsitemSubmission.setPublisher("A publisher")
    newNewsitemSubmission.setDescription("Something interesting")

    val publisher = Website(_id = BSONObjectID.generate(), title = "A publisher")
    when(mongoRepository.getWebsiteByName("A publisher")).thenReturn(Future.successful(Some(publisher)))
    when(urlCleaner.cleanSubmittedItemUrl(new URL("http://localhost/a-newsitem"))).thenReturn(new URL("http://localhost/a-newsitem"))
    when(contentUpdateService.create(any(classOf[Resource]))(any())).thenReturn(Future.successful(true))
    when(loggedInUserFilter.getLoggedInUser).thenReturn(Some(User()))

    val bindingResultWithNoErrors = mock(classOf[BindingResult])
    val createdNewsitem: ArgumentCaptor[Newsitem] = ArgumentCaptor.forClass(classOf[Newsitem])

    val request = new MockHttpServletRequest
    request.getSession().setAttribute("user", User())
    controller.submit(newNewsitemSubmission, bindingResultWithNoErrors, request)

    verify(contentUpdateService).create(createdNewsitem.capture)(ArgumentMatchers.eq(ec))
    assertEquals("A newsitem", createdNewsitem.getValue.title)
    assertEquals("http://localhost/a-newsitem", createdNewsitem.getValue.page)
    assertEquals(new DateTime(2020, 1, 22, 0, 0).toDate, createdNewsitem.getValue.date)
    assertEquals(publisher._id, createdNewsitem.getValue.publisher.get)
    assertEquals(Some("Something interesting"), createdNewsitem.getValue.description)
   }

}
