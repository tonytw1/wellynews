package nz.co.searchwellington.modification

import nz.co.searchwellington.model.Newsitem
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexer
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.repositories.{HandTaggingDAO, SuppressionDAO}
import org.junit.Test
import org.mockito.Mockito.{mock, verify, when}

import scala.concurrent.Future

class ContentDeletionServiceTest {

  private val suppressionDAO = mock(classOf[SuppressionDAO])
  private val mongoRepository = mock(classOf[MongoRepository])
  private val handTaggingDAO = mock(classOf[HandTaggingDAO])
  private val elasticSearchIndexer = mock(classOf[ElasticSearchIndexer])
  private val contentUpdateService = mock(classOf[ContentUpdateService])

  private val contentDeletionService = new ContentDeletionService(suppressionDAO, mongoRepository, handTaggingDAO, elasticSearchIndexer, contentUpdateService)

  @Test
  def shouldSuppressNewsitemUrlsWhenDeletingToStopThemFromBeenReaccepted() = {
    val newsitem = Newsitem(page = "http://localhost/some-page")
    when(elasticSearchIndexer.deleteResource(newsitem._id)).thenReturn(Future.successful(true))

    contentDeletionService.performDelete(newsitem)

    verify(suppressionDAO).addSuppression("http://localhost/some-page")
  }

}
