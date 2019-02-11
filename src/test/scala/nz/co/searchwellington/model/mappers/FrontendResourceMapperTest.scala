package nz.co.searchwellington.model.mappers

import nz.co.searchwellington.model.taggingvotes.TaggingVote
import nz.co.searchwellington.model.{Newsitem, Tag, UrlWordsGenerator}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import nz.co.searchwellington.views.GeocodeToPlaceMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    val tag = new Tag(id = 123)

    val frontedTag = mapper.mapTagToFrontendTag(tag)

    assertEquals(tag.id, frontedTag.id)
  }

  @Test
  def canMapNewsitemsToFrontendNewsitems(): Unit = {
    val newsitem = new Newsitem(id = 123)
    when(urlWordsGenerator.makeUrlForNewsitem(newsitem)).thenReturn(Some("some-url-words"))

    val frontendNewsitem = mapper.createFrontendResourceFrom(newsitem)

    assertEquals(newsitem.id, frontendNewsitem.id)
    assertEquals("some-url-words", frontendNewsitem.getUrlWords)
  }

  @Test
  def tagsShouldBeAppliedToFrontendNewsitems(): Unit = {
    val newsitem = new Newsitem(id = 123)
    when(urlWordsGenerator.makeUrlForNewsitem(newsitem)).thenReturn(Some("some-url-words"))

    val frontendTags = Seq.empty  // TODO non empty
    when(taggingReturnsOfficerService.compileTaggingVotes(newsitem)).thenReturn(frontendTags)

    val frontendNewsitem = mapper.createFrontendResourceFrom(newsitem)

    assertFalse(frontendNewsitem.tags.isEmpty)
  }

}