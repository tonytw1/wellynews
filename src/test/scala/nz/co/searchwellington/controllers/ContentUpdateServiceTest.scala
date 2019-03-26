package nz.co.searchwellington.controllers

import java.util.UUID

import nz.co.searchwellington.model.{Newsitem, Website}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.queues.LinkCheckerQueue
import nz.co.searchwellington.repositories.FrontendContentUpdater
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.{Before, Test}
import org.mockito.Mockito.{verify, when}
import org.mockito.{Mock, MockitoAnnotations}
import reactivemongo.bson.BSONObjectID

import scala.concurrent.Future

class ContentUpdateServiceTest {
  @Mock private val mongoReposity: MongoRepository = null
  @Mock private val linkCheckerQueue: LinkCheckerQueue = null
  private val resourceObjectId: BSONObjectID = BSONObjectID.generate()
  private val resourceId: String = UUID.randomUUID().toString
  private val exitingResource= Newsitem(id = resourceId, page = Some("http://test/abc"))
  private val updatedResource= Newsitem(id = resourceId , page = Some("http://test/123"))
  private val newResource = Website(id = "", page = Some("http://test/abc"))

  @Mock private val frontendContentUpdater: FrontendContentUpdater = null
  private var service: ContentUpdateService = null

  @Before
  def setUp {
    MockitoAnnotations.initMocks(this)
    when(mongoReposity.getResourceByObjectId(resourceObjectId)).thenReturn(Future.successful(Some(updatedResource)))
    service = new ContentUpdateService(mongoReposity, linkCheckerQueue, frontendContentUpdater)
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
