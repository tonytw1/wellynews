package nz.co.searchwellington.modification

import nz.co.searchwellington.model.{Newsitem, Watchlist}
import nz.co.searchwellington.repositories.SuppressionDAO
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexer
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, verify, when}
import reactivemongo.api.commands.WriteResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ContentDeletionServiceTest {

  private val suppressionDAO = mock(classOf[SuppressionDAO])
  private val mongoRepository = mock(classOf[MongoRepository])
  private val elasticSearchIndexer = mock(classOf[ElasticSearchIndexer])
  private val contentUpdateService = mock(classOf[ContentUpdateService])

  private val contentDeletionService = new ContentDeletionService(suppressionDAO, mongoRepository, elasticSearchIndexer, contentUpdateService)

  @Test
  def canDeleteResources(): Unit = {
    val watchlist = Watchlist(page = "http://localhost/some-page")

    when(elasticSearchIndexer.deleteResource(watchlist._id)).thenReturn(Future.successful(true))
    val successfulWrite = mock(classOf[WriteResult])
    when(successfulWrite.writeErrors).thenReturn(Seq.empty)
    when(mongoRepository.removeResource(watchlist)).thenReturn(Future.successful(successfulWrite))

    val result = contentDeletionService.performDelete(watchlist)

    assertTrue(result)
  }


  @Test
  def shouldSuppressNewsitemUrlsWhenDeletingToStopThemFromBeenReaccepted(): Unit = {
    val newsitem = Newsitem(page = "http://localhost/some-page")

    when(elasticSearchIndexer.deleteResource(newsitem._id)).thenReturn(Future.successful(true))
    val successfulWrite = mock(classOf[WriteResult])
    when(successfulWrite.writeErrors).thenReturn(Seq.empty)
    when(mongoRepository.removeResource(newsitem)).thenReturn(Future.successful(successfulWrite))
    when(suppressionDAO.addSuppression("http://localhost/some-page")).thenReturn(Future.successful(true))

    val result = contentDeletionService.performDelete(newsitem)

    assertTrue(result)
    verify(suppressionDAO).addSuppression("http://localhost/some-page")
  }

}
