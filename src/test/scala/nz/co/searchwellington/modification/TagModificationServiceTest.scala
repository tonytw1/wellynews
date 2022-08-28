package nz.co.searchwellington.modification

import nz.co.searchwellington.model.geo.Geocode
import nz.co.searchwellington.model.{Newsitem, Tag}
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexRebuildService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{HandTaggingService, TagDAO}
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, verify, when}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TagModificationServiceTest {

  private val tagDAO = mock(classOf[TagDAO])
  private val handTaggingService = mock(classOf[HandTaggingService])
  private val mongoRepository = mock(classOf[MongoRepository])
  private val elasticSearchIndexRebuildService = mock(classOf[ElasticSearchIndexRebuildService])

  private val tagModificationService = new TagModificationService(tagDAO, handTaggingService, mongoRepository, elasticSearchIndexRebuildService)

  private val tag = Tag(name = "Tag")

  @Test
  def tagDeletionsShouldDeleteTheTag(): Unit = {
    when(handTaggingService.clearTaggingsForTag(tag)).thenReturn(Future.successful(true))
    when(tagDAO.deleteTag(tag)).thenReturn(Future.successful(true))

    tagModificationService.deleteTag(tag)

    verify(tagDAO).deleteTag(tag)
  }

  @Test
  def tagDeletionShouldRemoveAllHandTaggingForTagTag(): Unit = {
    when(handTaggingService.clearTaggingsForTag(tag)).thenReturn(Future.successful(true))
    when(tagDAO.deleteTag(tag)).thenReturn(Future.successful(true))

    tagModificationService.deleteTag(tag)

    verify(handTaggingService).clearTaggingsForTag(tag)
  }

  @Test
  def changingTagsGeocodeShouldReindexAffectedResources(): Unit = {
    val updatedWithGeocode = tag.copy(geocode = Some(Geocode(address = Some("Somewhere"))))
    val taggedResource = Newsitem()

    when(mongoRepository.getResourceIdsByTag(tag)).thenReturn(Future.successful(Seq(taggedResource._id)))

    tagModificationService.updateAffectedResources(tag, updatedWithGeocode)

    verify(elasticSearchIndexRebuildService).reindexResources(Seq(taggedResource._id), 0, 1)
  }

}
