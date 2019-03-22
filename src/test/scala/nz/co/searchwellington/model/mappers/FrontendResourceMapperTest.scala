package nz.co.searchwellington.model.mappers

import java.util.UUID

import nz.co.searchwellington.model.taggingvotes.HandTagging
import nz.co.searchwellington.model.{Newsitem, Tag, UrlWordsGenerator}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import nz.co.searchwellington.views.GeocodeToPlaceMapper
import org.junit.Assert.{assertEquals, assertFalse}
import org.junit.Test
import org.mockito.Mockito.{mock, when}

class FrontendResourceMapperTest {

  val taggingReturnsOfficerService = mock(classOf[TaggingReturnsOfficerService])
  val urlWordsGenerator = mock(classOf[UrlWordsGenerator])
  val geocodeToPlaceMapper = mock(classOf[GeocodeToPlaceMapper])
  val mongoRepository = mock(classOf[MongoRepository])

  val mapper = new FrontendResourceMapper(taggingReturnsOfficerService, urlWordsGenerator, geocodeToPlaceMapper, mongoRepository)

  @Test
  def canMapTagToFrontendTag(): Unit = {
    val tag = new Tag(id = UUID.randomUUID().toString)

    val frontedTag = mapper.mapTagToFrontendTag(tag)

    assertEquals(tag.id, frontedTag.id)
  }

  @Test
  def canMapNewsitemsToFrontendNewsitems(): Unit = {
    val newsitem = new Newsitem(id = "123")
    when(urlWordsGenerator.makeUrlForNewsitem(newsitem)).thenReturn(Some("some-url-words"))
    val tag = Tag(id = UUID.randomUUID().toString, name = "123", display_name = "123")
    when(taggingReturnsOfficerService.getIndexTagsForResource(newsitem)).thenReturn(Set(tag))
    when(taggingReturnsOfficerService.getHandTagsForResource(newsitem)).thenReturn(Set[Tag]())

    val frontendNewsitem = mapper.createFrontendResourceFrom(newsitem)

    assertEquals(newsitem.id, frontendNewsitem.id)
    assertEquals("some-url-words", frontendNewsitem.getUrlWords)
  }

  @Test
  def tagsShouldBeAppliedToFrontendNewsitems(): Unit = {
    val newsitem = new Newsitem(id = "123")
    when(urlWordsGenerator.makeUrlForNewsitem(newsitem)).thenReturn(Some("some-url-words"))

    val tag = Tag(id = UUID.randomUUID().toString, name = "123", display_name = "123")
    when(taggingReturnsOfficerService.getIndexTagsForResource(newsitem)).thenReturn(Set(tag))
    when(taggingReturnsOfficerService.getHandTagsForResource(newsitem)).thenReturn(Set[Tag]())

    val frontendNewsitem = mapper.createFrontendResourceFrom(newsitem)

    assertFalse(frontendNewsitem.tags.isEmpty)
    assertEquals(tag.id, frontendNewsitem.tags.get(0).id)
  }

  @Test
  def handTaggingsShouldBeAppliedToFrontendNewsitems(): Unit = {
    val newsitem = new Newsitem(id = "123")
    when(urlWordsGenerator.makeUrlForNewsitem(newsitem)).thenReturn(Some("some-url-words"))

    val tag = Tag(id = UUID.randomUUID().toString, name = "123", display_name = "123")
    val tagging = new HandTagging(user = null, tag = tag)
    val taggingVotes: Set[HandTagging] = Set(tagging)

    when(taggingReturnsOfficerService.getIndexTagsForResource(newsitem)).thenReturn(Set[Tag]())
    when(taggingReturnsOfficerService.getHandTagsForResource(newsitem)).thenReturn(Set(tag))

    val frontendNewsitem = mapper.createFrontendResourceFrom(newsitem)

    assertFalse(frontendNewsitem.handTags.isEmpty)
    assertEquals(tag.id, frontendNewsitem.handTags.get(0).id)
  }

}