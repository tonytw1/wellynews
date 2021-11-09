package nz.co.searchwellington.model.mappers

import java.util.UUID
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.admin.AdminUrlBuilder
import nz.co.searchwellington.model.{Newsitem, SiteInformation, Tag, UrlWordsGenerator, User, Website}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.{IndexTagsService, TaggingReturnsOfficerService}
import nz.co.searchwellington.urls.UrlBuilder
import org.joda.time.{DateTime, DateTimeZone}
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.Test
import org.mockito.Mockito.{mock, when}
import reactivemongo.api.bson.BSONObjectID
import uk.co.eelpieconsulting.common.dates.DateFormatter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class FrontendResourceMapperTest extends ReasonableWaits {

  private val taggingReturnsOfficerService = mock(classOf[TaggingReturnsOfficerService])
  private val indexTagsService = mock(classOf[IndexTagsService])
  private val mongoRepository = mock(classOf[MongoRepository])

  private val urlBuilder = new UrlBuilder(new SiteInformation(), new UrlWordsGenerator(new DateFormatter(DateTimeZone.UTC)))
  private val adminUrlBuilder = new AdminUrlBuilder(urlBuilder, "", "")

  val mapper = new FrontendResourceMapper(taggingReturnsOfficerService, indexTagsService, mongoRepository, adminUrlBuilder)

  @Test
  def shouldMapNewsitemsToFrontendNewsitems(): Unit = {
    val owner = User(BSONObjectID.generate(), name = Some(UUID.randomUUID().toString), profilename = Some(UUID.randomUUID().toString))
    val newsitem = Newsitem(id = "123", http_status = 200, title = Some("Something happened today"),
      date = Some(new DateTime(2020, 10, 7, 12, 0, 0, 0).toDate),
      owner = Some(owner._id))
    when(taggingReturnsOfficerService.getHandTagsForResource(newsitem)).thenReturn(Future.successful(Seq.empty))
    when(indexTagsService.getIndexTagsForResource(newsitem)).thenReturn(Future.successful(Seq.empty))
    when(indexTagsService.getIndexGeocodeForResource(newsitem)).thenReturn(Future.successful(None))
    when(mongoRepository.getUserByObjectId(owner._id)).thenReturn(Future.successful(Some(owner)))

    val frontendNewsitem = Await.result(mapper.createFrontendResourceFrom(newsitem), TenSeconds)

    assertEquals(newsitem.id, frontendNewsitem.id)
    assertEquals(200, frontendNewsitem.httpStatus)
    assertEquals(owner.profilename.get, frontendNewsitem.getOwner)
  }

  @Test
  def handTaggingsShouldBeAppliedToFrontendResources(): Unit = {
    val website = Website(id = "123")

    val tag = Tag(id = UUID.randomUUID().toString, name = "123", display_name = "123")

    when(taggingReturnsOfficerService.getHandTagsForResource(website)).thenReturn(Future.successful(Seq(tag)))
    when(indexTagsService.getIndexTagsForResource(website)).thenReturn(Future.successful(Seq.empty))
    when(indexTagsService.getIndexGeocodeForResource(website)).thenReturn(Future.successful(None))

    val frontendWebsite = Await.result(mapper.createFrontendResourceFrom(website), TenSeconds)

    assertFalse(frontendWebsite.handTags.isEmpty)
    assertEquals(tag.id, frontendWebsite.handTags.head.id)
  }

  @Test
  def shouldApplyActionsToFrontendResourcesViewedByAdmins(): Unit = {
    val website = Website(id = "123")
    val adminUser = User(admin = true)

    when(taggingReturnsOfficerService.getHandTagsForResource(website)).thenReturn(Future.successful(Seq.empty))
    when(indexTagsService.getIndexTagsForResource(website)).thenReturn(Future.successful(Seq.empty))
    when(indexTagsService.getIndexGeocodeForResource(website)).thenReturn(Future.successful(None))

    val frontendWebsite = Await.result(mapper.createFrontendResourceFrom(website, loggedInUser = Some(adminUser)), TenSeconds)

    assertTrue(frontendWebsite.actions.nonEmpty)
    val editAction = frontendWebsite.actions.head
    assertEquals("Edit", editAction.label)
    assertEquals("/edit-website/123", editAction.link)
  }

  @Test
  def shouldApplyActionsToFrontendNewsitemsViewedByAdmins(): Unit = {
    val website = Newsitem(id = "456")
    val adminUser = User(admin = true)

    when(taggingReturnsOfficerService.getHandTagsForResource(website)).thenReturn(Future.successful(Seq.empty))
    when(indexTagsService.getIndexTagsForResource(website)).thenReturn(Future.successful(Seq.empty))
    when(indexTagsService.getIndexGeocodeForResource(website)).thenReturn(Future.successful(None))

    val frontendWebsite = Await.result(mapper.createFrontendResourceFrom(website, loggedInUser = Some(adminUser)), TenSeconds)

    assertTrue(frontendWebsite.actions.nonEmpty)
    val editAction = frontendWebsite.actions.head
    assertEquals("Edit", editAction.label)
    assertEquals("/edit-newsitem/456", editAction.link)
  }

  @Test
  def shouldNotApplyActionIfUserIsNotLoggedIn(): Unit = {
    val website = Website(id = "123")
    when(taggingReturnsOfficerService.getHandTagsForResource(website)).thenReturn(Future.successful(Seq.empty))
    when(indexTagsService.getIndexTagsForResource(website)).thenReturn(Future.successful(Seq.empty))
    when(indexTagsService.getIndexGeocodeForResource(website)).thenReturn(Future.successful(None))

    val frontendWebsite = Await.result(mapper.createFrontendResourceFrom(website, loggedInUser = None), TenSeconds)

    assertTrue(frontendWebsite.actions.isEmpty)
  }

}
