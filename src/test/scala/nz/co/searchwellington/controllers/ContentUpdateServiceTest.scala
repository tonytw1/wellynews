package nz.co.searchwellington.controllers

import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import nz.co.searchwellington.model.Newsitem
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
  @Mock private val exitingResource: Newsitem = null
  @Mock private val updatedResource: Newsitem = null
  @Mock private val newResource: Newsitem = null
  @Mock private val frontendContentUpdater: FrontendContentUpdater = null
  private var service: ContentUpdateService = null

  @Before
  @throws[Exception]
  def setUp {
    MockitoAnnotations.initMocks(this)
    when(exitingResource.getId).thenReturn(1)
    when(exitingResource.getType).thenReturn("N")
    when(exitingResource.getUrl).thenReturn("http://test/abc")
    when(updatedResource.getId).thenReturn(1)
    when(updatedResource.getType).thenReturn("N")
    when(updatedResource.getUrl).thenReturn("http://test/123")
    when(newResource.getId).thenReturn(0)
    when(newResource.getType).thenReturn("W")
    when(resourceDAO.loadResourceById(1)).thenReturn(Some(updatedResource))
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