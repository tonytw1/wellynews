package nz.co.searchwellington.controllers

import java.util.UUID

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Newsitem
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.queues.LinkCheckerQueue
import nz.co.searchwellington.repositories.FrontendContentUpdater
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.Test
import org.mockito.Mockito.{mock, verify, when}
import reactivemongo.api.commands.UpdateWriteResult

import scala.concurrent.{Await, Future}

class ContentUpdateServiceTest extends ReasonableWaits {

  private val mongoRepository = mock(classOf[MongoRepository])
  private val linkCheckerQueue = mock(classOf[LinkCheckerQueue])
  private val frontendContentUpdater = mock(classOf[FrontendContentUpdater])

  private val resourceId = UUID.randomUUID().toString
  private val updatedResource = Newsitem(id = resourceId, page = Some("http://test/123"))

  private val service = new ContentUpdateService(mongoRepository, linkCheckerQueue, frontendContentUpdater)
  private val successfulUpdateResult = mock(classOf[UpdateWriteResult])

  @Test
  def shouldPeristUpdatesInMongo {
    when(mongoRepository.saveResource(updatedResource)).thenReturn(Future.successful(successfulUpdateResult))

    Await.result(service.update(updatedResource), TenSeconds)

    verify(mongoRepository).saveResource(updatedResource)
  }

  @Test
  def shouldUpdateTheElasticsearchIndexWhenUpdating {
    when(mongoRepository.saveResource(updatedResource)).thenReturn(Future.successful(successfulUpdateResult))

    Await.result(service.update(updatedResource), TenSeconds)

    verify(frontendContentUpdater).update(updatedResource)
  }

}
