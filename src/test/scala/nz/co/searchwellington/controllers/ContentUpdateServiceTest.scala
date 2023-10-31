package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.linkchecking.LinkCheckRequest
import nz.co.searchwellington.model.{Newsitem, Website}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.queues.{ElasticIndexQueue, LinkCheckerQueue}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, verify, when}
import reactivemongo.api.commands.WriteResult

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class ContentUpdateServiceTest extends ReasonableWaits {

  private val mongoRepository = mock(classOf[MongoRepository])
  private val elasticIndexQueue = mock(classOf[ElasticIndexQueue])
  private val linkCheckerQueue = mock(classOf[LinkCheckerQueue])

  private val resourceId = UUID.randomUUID().toString
  private val updatedResource = Newsitem(id = resourceId, page = "http://test/123")

  private val service = new ContentUpdateService(mongoRepository, elasticIndexQueue, linkCheckerQueue)
  private val successfulUpdateResult = mock(classOf[WriteResult])

  @Test
  def shouldPersistUpdatesInMongo(): Unit = {
    when(mongoRepository.getResourceByObjectId(updatedResource._id)).thenReturn(Future.successful(None))
    when(mongoRepository.saveResource(updatedResource)).thenReturn(Future.successful(successfulUpdateResult))
    when(elasticIndexQueue.add(updatedResource)).thenReturn(true)

    Await.result(service.update(updatedResource), TenSeconds)

    verify(mongoRepository).saveResource(updatedResource)
  }

  @Test
  def shouldUpdateTheElasticsearchIndexWhenUpdating(): Unit = {
    when(mongoRepository.getResourceByObjectId(updatedResource._id)).thenReturn(Future.successful(None))
    when(mongoRepository.saveResource(updatedResource)).thenReturn(Future.successful(successfulUpdateResult))
    when(elasticIndexQueue.add(updatedResource)).thenReturn(true)

    Await.result(service.update(updatedResource), TenSeconds)

    verify(elasticIndexQueue).add(updatedResource)
  }

  @Test
  def shouldQueueNewlyCreatedResourcesForLinkChecking(): Unit = {
    val newResource = Website()
    when(mongoRepository.saveResource(newResource)).thenReturn(Future.successful(successfulUpdateResult))
    when(elasticIndexQueue.add(newResource)).thenReturn(true)
    when(successfulUpdateResult.writeErrors).thenReturn(Seq.empty)

    Await.result(service.create(newResource), TenSeconds)

    verify(linkCheckerQueue).add(LinkCheckRequest(newResource._id.stringify, None))
  }

  @Test
  def shouldQueueUpdatedUrlsForLinkChecking(): Unit = {
    val resource = Website(page = "http://localhost/old-url", last_scanned = Some(DateTime.now().toDate))
    val updatedResource = resource.copy(page = "http://localhost/new-url")
    when(mongoRepository.getResourceByObjectId(updatedResource._id)).thenReturn(Future.successful(Some(resource)))
    when(mongoRepository.saveResource(updatedResource)).thenReturn(Future.successful(successfulUpdateResult))
    when(elasticIndexQueue.add(updatedResource)).thenReturn(true)
    when(successfulUpdateResult.writeErrors).thenReturn(Seq.empty)

    Await.result(service.update(updatedResource), TenSeconds)

    assertEquals(resource._id, updatedResource._id)
    verify(linkCheckerQueue).add(LinkCheckRequest(updatedResource._id.stringify, updatedResource.last_scanned))
  }

}
