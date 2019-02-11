package nz.co.searchwellington.model.mappers

import nz.co.searchwellington.model.{Tag, UrlWordsGenerator}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService
import nz.co.searchwellington.views.GeocodeToPlaceMapper
import org.junit.Test
import org.junit.Assert.assertEquals

import org.mockito.Mockito.mock

class FrontendResourceMapperTest {

  val taggingReturnsOfficerService = mock(classOf[TaggingReturnsOfficerService])
  val urlWordsGenerator = mock(classOf[UrlWordsGenerator])
  val geocodeToPlaceMapper = mock(classOf[GeocodeToPlaceMapper])
  val mongoRepository = mock(classOf[MongoRepository])

  @Test
  def canMappingTagToFrontendTag(): Unit = {
    val tag = new Tag(id = 123)

    val mapper = new FrontendResourceMapper(taggingReturnsOfficerService, urlWordsGenerator, geocodeToPlaceMapper, mongoRepository)

    val frontedTag = mapper.mapTagToFrontendTag(tag)

    assertEquals(tag.id, frontedTag.id)
  }

}