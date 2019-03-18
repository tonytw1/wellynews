package nz.co.searchwellington.controllers

import java.util.UUID

import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import nz.co.searchwellington.model.{Newsitem, Website}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.queues.LinkCheckerQueue
import nz.co.searchwellington.repositories.FrontendContentUpdater
import nz.co.searchwellington.repositories.HibernateResourceDAO
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class ContentUpdateServiceTest {
  @Mock private val resourceDAO: HibernateResourceDAO = null
  @Mock private val linkCheckerQueue: LinkCheckerQueue = null
  private val resourceId: String = UUID.randomUUID().toString
  private val exitingResource= Newsitem(id = resourceId, page = Some("http://test/abc"))
  private val updatedResource= Newsitem(id = resourceId , page = Some("http://test/123"))
  private val newResource = Website(id = "", page = Some("http://test/abc"))

  @Mock private val frontendContentUpdater: FrontendContentUpdater = null
  private var service: ContentUpdateService = null

  @Before
  @throws[Exception]
  def setUp {
    MockitoAnnotations.initMocks(this)
    when(resourceDAO.loadResourceById(resourceId)).thenReturn(Some(updatedResource))
    service = new ContentUpdateService(resourceDAO, linkCheckerQueue, frontendContentUpdater)
  }

  @Test
  @throws[Exception]
  def shouldSaveThroughTheHibernateDAO {
    service.update(updatedResource)
    verify(resourceDAO).saveResource(updatedResource)
  }

  @Test
  @throws[Exception]
  def shouldUpdateTheFrontendSolrIndexOnSave {
    service.update(updatedResource)
    verify(frontendContentUpdater).update(updatedResource)
  }

}
