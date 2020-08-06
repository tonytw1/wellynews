package nz.co.searchwellington.model.mappers

import java.util.UUID

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.admin.AdminUrlBuilder
import nz.co.searchwellington.model.{Newsitem, Tag, UrlWordsGenerator, User, Website}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import org.joda.time.DateTime
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.Test
import org.mockito.Mockito.{mock, when}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class FrontendResourceMapperTest extends ReasonableWaits {

  private val taggingReturnsOfficerService = mock(classOf[TaggingReturnsOfficerService])
  private val urlWordsGenerator = new UrlWordsGenerator
  private val mongoRepository = mock(classOf[MongoRepository])
  private val adminUrlBuilder = mock(classOf[AdminUrlBuilder])

  val mapper = new FrontendResourceMapper(taggingReturnsOfficerService, urlWordsGenerator, mongoRepository, adminUrlBuilder)

  @Test
  def canMapNewsitemsToFrontendNewsitems(): Unit = {
    val newsitem = Newsitem(id = "123", http_status = 200, title = Some("Something happened today"),
      date = Some(new DateTime(2020, 10, 7, 12, 0, 0, 0).toDate))
    when(taggingReturnsOfficerService.getHandTagsForResource(newsitem)).thenReturn(Future.successful(Seq.empty))
    when(taggingReturnsOfficerService.getIndexTagsForResource(newsitem)).thenReturn(Future.successful(Seq.empty))
    when(taggingReturnsOfficerService.getIndexGeocodeForResource(newsitem)).thenReturn(Future.successful(None))

    val frontendNewsitem = Await.result(mapper.createFrontendResourceFrom(newsitem), TenSeconds)

    assertEquals(newsitem.id, frontendNewsitem.id)
    assertEquals(200, frontendNewsitem.httpStatus)
  }

  @Test
  def handTaggingsShouldBeAppliedToFrontendResources(): Unit = {
    val website = Website(id = "123")

    val tag = Tag(id = UUID.randomUUID().toString, name = "123", display_name = "123")

    when(taggingReturnsOfficerService.getHandTagsForResource(website)).thenReturn(Future.successful(Seq(tag)))
    when(taggingReturnsOfficerService.getIndexTagsForResource(website)).thenReturn(Future.successful(Seq.empty))
    when(taggingReturnsOfficerService.getIndexGeocodeForResource(website)).thenReturn(Future.successful(None))

    val frontendWebsite = Await.result(mapper.createFrontendResourceFrom(website), TenSeconds)

    assertFalse(frontendWebsite.handTags.isEmpty)
    assertEquals(tag.id, frontendWebsite.handTags.head.id)
  }

  @Test
  def shouldApplyActionsToFrontendResourcesViewedByAdmins(): Unit = {
    val website = Website(id = "123")
    val adminUser = User(admin = true)

    when(taggingReturnsOfficerService.getHandTagsForResource(website)).thenReturn(Future.successful(Seq.empty))
    when(taggingReturnsOfficerService.getIndexTagsForResource(website)).thenReturn(Future.successful(Seq.empty))
    when(taggingReturnsOfficerService.getIndexGeocodeForResource(website)).thenReturn(Future.successful(None))

    val frontendWebsite = Await.result(mapper.createFrontendResourceFrom(website, loggedInUser = Some(adminUser)), TenSeconds)

    assertTrue(frontendWebsite.actions.nonEmpty)
  }

}