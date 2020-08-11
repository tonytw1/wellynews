package nz.co.searchwellington.controllers

import nz.co.searchwellington.forms.EditNewsitem
import nz.co.searchwellington.geocoding.osm.CachingNominatimResolveOsmIdService
import nz.co.searchwellington.model.{Newsitem, User}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{FrontendContentUpdater, HandTaggingService, TagDAO}
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.{mock, verify, when}
import org.mockito.{ArgumentCaptor, Matchers}
import org.springframework.validation.BindingResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EditNewsitemControllerTest {

  val contentUpdateService = mock(classOf[ContentUpdateService])
  val mongoRepository = mock(classOf[MongoRepository])
  val loggedInUserFilter = mock(classOf[LoggedInUserFilter])
  val tagDAO = mock(classOf[TagDAO])
  val cachingNominatimResolveOsmIdService = mock(classOf[CachingNominatimResolveOsmIdService])
  val handTaggingService = new HandTaggingService(mock(classOf[FrontendContentUpdater]), mongoRepository)

  val controller = new EditNewsitemController(contentUpdateService, mongoRepository, loggedInUserFilter, tagDAO, cachingNominatimResolveOsmIdService, handTaggingService)

  @Test
  def canUpdateExistingNewsitem(): Unit = {
    val existingNewsitem = Newsitem(id = "123")

    val adminUser = User(admin = true)
    when(loggedInUserFilter.getLoggedInUser).thenReturn(Some(adminUser))
    when(mongoRepository.getResourceById(existingNewsitem.id)).thenReturn(Future.successful(Some(existingNewsitem)))
    when(tagDAO.loadTagsById(Seq.empty)).thenReturn(Future.successful(Seq.empty))

    val editFormSubmission: EditNewsitem = new EditNewsitem()
    editFormSubmission.setTitle("New title")
    import scala.collection.JavaConverters._
    editFormSubmission.setTags(Seq.empty.asJava)

    controller.submit(existingNewsitem.id, editFormSubmission, mock(classOf[BindingResult]))

    val updatedNewsitem = ArgumentCaptor.forClass(classOf[Newsitem])
    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
    verify(contentUpdateService).update(updatedNewsitem.capture)(Matchers.eq(ec))

    assertEquals(Some("New title"), updatedNewsitem.getValue.title)
  }

}
