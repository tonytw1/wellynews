package nz.co.searchwellington.model.mappers

import java.util.UUID

import nz.co.searchwellington.model.{Newsitem, Tag, UrlWordsGenerator}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import nz.co.searchwellington.views.GeocodeToPlaceMapper
import org.junit.Assert.{assertEquals, assertFalse, assertTrue}
import org.junit.Test
import org.mockito.Mockito.{mock, when}

class FrontendResourceMapperTest {

  private val taggingReturnsOfficerService = mock(classOf[TaggingReturnsOfficerService])
  private val urlWordsGenerator = mock(classOf[UrlWordsGenerator])
  private val geocodeToPlaceMapper = mock(classOf[GeocodeToPlaceMapper])
  private val mongoRepository = mock(classOf[MongoRepository])

  val mapper = new FrontendResourceMapper(taggingReturnsOfficerService, urlWordsGenerator, geocodeToPlaceMapper, mongoRepository)

  @Test
  def canMapNewsitemsToFrontendNewsitems(): Unit = {
    val newsitem = Newsitem(id = "123", http_status = 200)
    when(urlWordsGenerator.makeUrlForNewsitem(newsitem)).thenReturn(Some("some-url-words"))
    when(taggingReturnsOfficerService.getHandTagsForResource(newsitem)).thenReturn(Seq.empty)
    when(taggingReturnsOfficerService.getIndexGeocodeForResource(newsitem)).thenReturn(None)

    val frontendNewsitem = mapper.createFrontendResourceFrom(newsitem)

    assertEquals(newsitem.id, frontendNewsitem.id)
    assertEquals("some-url-words", frontendNewsitem.getUrlWords)
    assertEquals(200, frontendNewsitem.httpStatus)
  }

  @Test
  def handTaggingsShouldBeAppliedToFrontendNewsitems(): Unit = {
    val newsitem = Newsitem(id = "123")
    when(urlWordsGenerator.makeUrlForNewsitem(newsitem)).thenReturn(Some("some-url-words"))

    val tag = Tag(id = UUID.randomUUID().toString, name = "123", display_name = "123")

    when(taggingReturnsOfficerService.getHandTagsForResource(newsitem)).thenReturn(Seq(tag))
    when(taggingReturnsOfficerService.getIndexGeocodeForResource(newsitem)).thenReturn(None)

    val frontendNewsitem = mapper.createFrontendResourceFrom(newsitem)

    assertFalse(frontendNewsitem.handTags.isEmpty)
    assertEquals(tag.id, frontendNewsitem.handTags.get(0).id)
  }

  @Test
  def indexTagsShouldNotBeAppliedToFrontendNewsitems(): Unit = {
    val newsitem = Newsitem(id = "123")
    when(urlWordsGenerator.makeUrlForNewsitem(newsitem)).thenReturn(Some("some-url-words"))

    val tag = Tag(id = UUID.randomUUID().toString, name = "123", display_name = "123")
    when(taggingReturnsOfficerService.getIndexTagsForResource(newsitem)).thenReturn(Seq(tag))
    when(taggingReturnsOfficerService.getHandTagsForResource(newsitem)).thenReturn(Seq[Tag]())
    when(taggingReturnsOfficerService.getIndexGeocodeForResource(newsitem)).thenReturn(None)

    val frontendNewsitem = mapper.createFrontendResourceFrom(newsitem)

    assertTrue(frontendNewsitem.tags.isEmpty)
  }

}