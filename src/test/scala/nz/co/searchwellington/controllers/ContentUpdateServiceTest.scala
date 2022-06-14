package nz.co.searchwellington.controllers

import java.util.UUID
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.{Newsitem, Website}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.queues.LinkCheckerQueue
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexRebuildService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, verify, when}
import reactivemongo.api.commands.WriteResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class ContentUpdateServiceTest extends ReasonableWaits {

  private val mongoRepository = mock(classOf[MongoRepository])
  private val elasticSearchIndexRebuildService = mock(classOf[ElasticSearchIndexRebuildService])
  private val linkCheckerQueue = mock(classOf[LinkCheckerQueue])

  private val resourceId = UUID.randomUUID().toString
  private val updatedResource = Newsitem(id = resourceId, page = "http://test/123")

  private val service = new ContentUpdateService(mongoRepository, elasticSearchIndexRebuildService, linkCheckerQueue)
  private val successfulUpdateResult = mock(classOf[WriteResult])

  @Test
  def shouldPersistUpdatesInMongo(): Unit = {
    when(mongoRepository.saveResource(updatedResource)).thenReturn(Future.successful(successfulUpdateResult))
    when(elasticSearchIndexRebuildService.index(updatedResource)).thenReturn(Future.successful(true))

    Await.result(service.update(updatedResource), TenSeconds)

    verify(mongoRepository).saveResource(updatedResource)
  }

  @Test
  def shouldUpdateTheElasticsearchIndexWhenUpdating(): Unit = {
    when(mongoRepository.saveResource(updatedResource)).thenReturn(Future.successful(successfulUpdateResult))
    when(elasticSearchIndexRebuildService.index(updatedResource)).thenReturn(Future.successful(true))

    Await.result(service.update(updatedResource), TenSeconds)

    verify(elasticSearchIndexRebuildService).index(updatedResource)
  }

  @Test
  def shouldQueueNewlyCreatedResourcesForLinkChecking(): Unit = {
    val newResource = Website()
    when(mongoRepository.saveResource(newResource)).thenReturn(Future.successful(successfulUpdateResult))
    when(elasticSearchIndexRebuildService.index(newResource)).thenReturn(Future.successful(true))

    Await.result(service.create(newResource), TenSeconds)

    verify(linkCheckerQueue).add(newResource._id.stringify)
  }

}
