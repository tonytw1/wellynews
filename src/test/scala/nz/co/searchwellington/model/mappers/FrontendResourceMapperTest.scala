package nz.co.searchwellington.model.mappers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.admin.AdminUrlBuilder
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.taggingvotes.HandTagging
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.{IndexTagsService, TaggingReturnsOfficerService}
import nz.co.searchwellington.urls.UrlBuilder
import org.joda.time.{DateTime, DateTimeZone}
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.Test
import org.mockito.Mockito.{mock, when}
import reactivemongo.api.bson.BSONObjectID
import uk.co.eelpieconsulting.common.dates.DateFormatter

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class FrontendResourceMapperTest extends ReasonableWaits {

  private val indexTagsService = mock(classOf[IndexTagsService])
  private val mongoRepository = mock(classOf[MongoRepository])
  private val taggingReturnsOfficerService = mock(classOf[TaggingReturnsOfficerService])

  private val urlBuilder = new UrlBuilder(new SiteInformation(), new UrlWordsGenerator(new DateFormatter(DateTimeZone.UTC)))
  private val adminUrlBuilder = new AdminUrlBuilder(urlBuilder, "", "")

  private val frontendResourceMapper = new FrontendResourceMapper(indexTagsService, mongoRepository, adminUrlBuilder, taggingReturnsOfficerService)

  @Test
  def shouldMapNewsitemsToFrontendNewsitems(): Unit = {
    val owner = User(BSONObjectID.generate(), name = Some(UUID.randomUUID().toString), profilename = Some(UUID.randomUUID().toString))
    val newsitem = Newsitem(id = "123", http_status = 200, title = Some("Something happened today"),
      date = Some(new DateTime(2020, 10, 7, 12, 0, 0, 0).toDate),
      owner = Some(owner._id))

    when(taggingReturnsOfficerService.getTaggingsVotesForResource(newsitem)).thenReturn(Future.successful(Seq.empty))
    when(indexTagsService.getIndexGeocodeForResource(newsitem)).thenReturn(Future.successful(None))
    when(mongoRepository.getUserByObjectId(owner._id)).thenReturn(Future.successful(Some(owner)))

    val frontendNewsitem = Await.result(frontendResourceMapper.createFrontendResourceFrom(newsitem), TenSeconds)

    assertEquals(newsitem.id, frontendNewsitem.id)
    assertEquals(200, frontendNewsitem.httpStatus)
    assertEquals(owner.profilename.get, frontendNewsitem.getOwner)
  }

  @Test
  def handTaggingsShouldBeAppliedToFrontendResources(): Unit = {
    val owner = User(BSONObjectID.generate(), name = Some(UUID.randomUUID().toString), profilename = Some(UUID.randomUUID().toString))
    val tag = Tag(id = UUID.randomUUID().toString, name = "123", display_name = "123")

    val tagging = Tagging(tag_id = tag._id, user_id = owner._id)
    val website = Website(id = "123", resource_tags = Seq(tagging))

    when(taggingReturnsOfficerService.getTaggingsVotesForResource(website)).thenReturn(Future.successful(Seq(HandTagging(user = owner, tag = tag))))
    when(indexTagsService.getIndexGeocodeForResource(website)).thenReturn(Future.successful(None))

    val frontendWebsite = Await.result(frontendResourceMapper.createFrontendResourceFrom(website), TenSeconds)

    assertFalse(frontendWebsite.handTags.isEmpty)
    assertEquals(tag.id, frontendWebsite.handTags.get.head.id)
  }

  @Test
  def shouldApplyActionsToFrontendResourcesViewedByAdmins(): Unit = {
    val website = Website(id = "123")
    val adminUser = User(admin = true)

    when(taggingReturnsOfficerService.getTaggingsVotesForResource(website)).thenReturn(Future.successful(Seq.empty))
    when(indexTagsService.getIndexGeocodeForResource(website)).thenReturn(Future.successful(None))

    val frontendWebsite = Await.result(frontendResourceMapper.createFrontendResourceFrom(website, loggedInUser = Some(adminUser)), TenSeconds)

    assertTrue(frontendWebsite.actions.nonEmpty)
    val editAction = frontendWebsite.actions.head
    assertEquals("Edit", editAction.label)
    assertEquals("/edit-website/123", editAction.link)
  }

  @Test
  def shouldApplyActionsToFrontendNewsitemsViewedByAdmins(): Unit = {
    val website = Newsitem(id = "456")
    val adminUser = User(admin = true)

    when(taggingReturnsOfficerService.getTaggingsVotesForResource(website)).thenReturn(Future.successful(Seq.empty))
    when(indexTagsService.getIndexGeocodeForResource(website)).thenReturn(Future.successful(None))

    val frontendWebsite = Await.result(frontendResourceMapper.createFrontendResourceFrom(website, loggedInUser = Some(adminUser)), TenSeconds)

    assertTrue(frontendWebsite.actions.nonEmpty)
    val editAction = frontendWebsite.actions.head
    assertEquals("Edit", editAction.label)
    assertEquals("/edit-newsitem/456", editAction.link)
  }

  @Test
  def shouldNotApplyActionIfUserIsNotLoggedIn(): Unit = {
    val website = Website(id = "123")
    when(taggingReturnsOfficerService.getTaggingsVotesForResource(website)).thenReturn(Future.successful(Seq.empty))
    when(indexTagsService.getIndexGeocodeForResource(website)).thenReturn(Future.successful(None))

    val frontendWebsite = Await.result(frontendResourceMapper.createFrontendResourceFrom(website, loggedInUser = None), TenSeconds)

    assertTrue(frontendWebsite.actions.isEmpty)
  }

}
