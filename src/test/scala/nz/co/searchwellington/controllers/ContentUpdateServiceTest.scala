package nz.co.searchwellington.controllers

import java.util.UUID

import nz.co.searchwellington.model.{Newsitem, Website}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.queues.LinkCheckerQueue
import nz.co.searchwellington.repositories.FrontendContentUpdater
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.{Before, Test}
import org.mockito.Mockito.{mock, verify, when}
import reactivemongo.bson.BSONObjectID

import scala.concurrent.Future

class ContentUpdateServiceTest {

  private val mongoReposity = mock(classOf[MongoRepository])
  private val linkCheckerQueue = mock(classOf[LinkCheckerQueue])
  private val frontendContentUpdater = mock(classOf[FrontendContentUpdater])

  private val resourceObjectId: BSONObjectID = BSONObjectID.generate()
  private val resourceId: String = UUID.randomUUID().toString
  private val exitingResource= Newsitem(id = resourceId, page = Some("http://test/abc"))
  private val updatedResource= Newsitem(id = resourceId , page = Some("http://test/123"))
  private val newResource = Website(id = "", page = Some("http://test/abc"))

  private var service =  new ContentUpdateService(mongoReposity, linkCheckerQueue, frontendContentUpdater)

  @Before
  def setUp {
    when(mongoReposity.getResourceByObjectId(resourceObjectId)).thenReturn(Future.successful(Some(updatedResource)))
  }

  @Test
  def shouldPeristUpdatesInMongo {
    service.update(updatedResource)
    verify(mongoReposity).saveResource(updatedResource)
  }

  @Test
  def shouldUpdateTheFrontendSolrIndexOnSave {
    service.update(updatedResource)
    verify(frontendContentUpdater).update(updatedResource)
  }

}
