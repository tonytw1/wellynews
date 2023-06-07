package nz.co.searchwellington.controllers

import nz.co.searchwellington.forms.EditNewsitem
import nz.co.searchwellington.geocoding.osm.GeoCodeService
import nz.co.searchwellington.model.{Newsitem, User, Website}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{ContentRetrievalService, HandTaggingService, TagDAO}
import nz.co.searchwellington.urls.UrlCleaner
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, verify, when}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.springframework.validation.BindingResult

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

class EditNewsitemControllerTest {

  private val contentUpdateService = mock(classOf[ContentUpdateService])
  private val mongoRepository = mock(classOf[MongoRepository])
  private val loggedInUserFilter = mock(classOf[LoggedInUserFilter])
  private val tagDAO = mock(classOf[TagDAO])
  private val contentRetrievalService: ContentRetrievalService = mock(classOf[ContentRetrievalService])
  private val geoCodeService = mock(classOf[GeoCodeService])
  private val handTaggingService = new HandTaggingService(contentUpdateService, mongoRepository)
  private val urlCleaner = mock(classOf[UrlCleaner])

  private implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  private val controller = new EditNewsitemController(contentUpdateService, mongoRepository, loggedInUserFilter, tagDAO,
    contentRetrievalService, geoCodeService, handTaggingService, urlCleaner)

  @Test
  def shouldUpdateExistingNewsitem(): Unit = {
    val existingNewsitem = Newsitem(id = "123")

    val adminUser = User(admin = true)
    when(loggedInUserFilter.getLoggedInUser).thenReturn(Some(adminUser))
    when(mongoRepository.getResourceById(existingNewsitem.id)).thenReturn(Future.successful(Some(existingNewsitem)))
    when(tagDAO.loadTagsById(Seq.empty)).thenReturn(Future.successful(Seq.empty))

    val anotherPublisher = Website(title = "Another publisher")
    when(mongoRepository.getWebsiteByName("Another publisher")).thenReturn(Future.successful(Some(anotherPublisher)))

    val editFormSubmission = new EditNewsitem()
    editFormSubmission.setTitle("New title")
    editFormSubmission.setDate("20200617")
    editFormSubmission.setPublisher("Another publisher")

    editFormSubmission.setTags(Seq.empty.asJava)

    controller.submit(existingNewsitem.id, editFormSubmission, mock(classOf[BindingResult]))

    val updatedNewsitem: ArgumentCaptor[Newsitem] = ArgumentCaptor.forClass(classOf[Newsitem])
    verify(contentUpdateService).update(updatedNewsitem.capture)(ArgumentMatchers.eq(ec))

    assertEquals("New title", updatedNewsitem.getValue.title)
    assertEquals(Some(new DateTime(2020, 6, 17, 0, 0, 0).toDate), updatedNewsitem.getValue.date)
    assertEquals(Some(anotherPublisher._id), updatedNewsitem.getValue.getPublisher)
  }

}
