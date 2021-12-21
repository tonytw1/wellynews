package nz.co.searchwellington.linkchecking

import io.micrometer.core.instrument.MeterRegistry
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.http.RobotsAwareHttpFetcher
import nz.co.searchwellington.model.Website
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.Assert.assertFalse
import org.junit.Test
import org.mockito.Mockito.mock

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters._

class LinkCheckerTest extends ReasonableWaits {

  private val mongoRepository = mock(classOf[MongoRepository])
  private val contentUpdateService = mock(classOf[ContentUpdateService])
  private  val httpFetcher = mock(classOf[RobotsAwareHttpFetcher])
  private val feedAutodiscoveryProcesser = mock(classOf[FeedAutodiscoveryProcesser])
  private val twitterPhotoDetector = mock(classOf[TwitterPhotoDetector])
  private val contentHasChangedProcesser = mock(classOf[ContentHasChangedProcesser])
  private val meterRegistry = mock(classOf[MeterRegistry])

  @Test
  def returnsFalseForUnparsableResourceUrls(): Unit = {
    val website = Website(page = "http:////feedproxy.google.com/~r/WellingtonGreens/~3/Tdcpif4nmKI/")

    val linkChecker = new LinkChecker(mongoRepository, contentUpdateService, httpFetcher, meterRegistry, Seq(feedAutodiscoveryProcesser, twitterPhotoDetector, contentHasChangedProcesser).asJava)

    val eventualResult = linkChecker.checkResource(website)

    assertFalse(Await.result(eventualResult, TenSeconds))
  }
}
