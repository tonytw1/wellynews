package nz.co.searchwellington.modification

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.geo.Geocode
import nz.co.searchwellington.model.{Newsitem, Tag}
import nz.co.searchwellington.queues.ElasticIndexQueue
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{HandTaggingService, TagDAO}
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, verify, when}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class TagModificationServiceTest extends ReasonableWaits {

  private val tagDAO = mock(classOf[TagDAO])
  private val handTaggingService = mock(classOf[HandTaggingService])
  private val mongoRepository = mock(classOf[MongoRepository])
  private val elasticIndexQueue = mock(classOf[ElasticIndexQueue])

  private val tagModificationService = new TagModificationService(tagDAO, handTaggingService, mongoRepository, elasticIndexQueue)

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
  def changingTagsGeocodeShouldRequestReindexOfAffectedResources(): Unit = {
    val updatedWithGeocode = tag.copy(geocode = Some(Geocode(address = Some("Somewhere"))))
    val taggedResource = Newsitem()

    when(mongoRepository.getResourceIdsByTag(tag)).thenReturn(Future.successful(Seq(taggedResource._id)))

    Await.result(tagModificationService.updateAffectedResources(tag, updatedWithGeocode), TenSeconds)

    verify(elasticIndexQueue).add(taggedResource._id)
  }

}
