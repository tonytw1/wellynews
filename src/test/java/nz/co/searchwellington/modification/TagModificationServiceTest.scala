package nz.co.searchwellington.modification

import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.repositories.HandTaggingService
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexRebuildService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.{mock, verify, when}
import org.mockito.MockitoAnnotations

import scala.concurrent.Future

class TagModificationServiceTest {

  private val tagDAO = mock(classOf[TagDAO])
  private val handTaggingService = mock(classOf[HandTaggingService])
  private val mongoRepository = mock(classOf[MongoRepository])
  private val elasticSearchIndexRebuildService = mock(classOf[ElasticSearchIndexRebuildService])

  private val tagModificationService =  new TagModificationService(tagDAO, handTaggingService, mongoRepository, elasticSearchIndexRebuildService)

  private val tag = Tag(name = "Tag")

  @Test
  def tagDeletionShouldResultInTheRemovalOfTheTag(): Unit = {
    when(handTaggingService.clearTaggingsForTag(tag)).thenReturn(Future.successful(true))
    when(tagDAO.deleteTag(tag)).thenReturn(Future.successful(true))

    tagModificationService.deleteTag(tag)

    verify(tagDAO).deleteTag(tag)
  }

  @Test
  def tagDeletionShouldResultInTheRemovalOfAllHandTaggingVotesForThatTag():Unit = {
    when(handTaggingService.clearTaggingsForTag(tag)).thenReturn(Future.successful(true))
    when(tagDAO.deleteTag(tag)).thenReturn(Future.successful(true))

    tagModificationService.deleteTag(tag)

    verify(handTaggingService).clearTaggingsForTag(tag)
  }

}