package nz.co.searchwellington.linkchecking

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.linkchecking.cards.SocialImageDetector
import nz.co.searchwellington.model.{Newsitem, Resource}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{Await, ExecutionContext, Future}

@Component
class TwitterPhotoDetector @Autowired()(mongoRepository: MongoRepository, socialImageDetector: SocialImageDetector)
  extends LinkCheckerProcessor with ReasonableWaits {

  private val log = LogFactory.getLog(classOf[TwitterPhotoDetector])

  override def process(checkResource: Resource, maybePageContent: Option[String], seen: DateTime)(implicit ec: ExecutionContext): Future[Boolean] = {
    val imageDetectedImageUrls = maybePageContent.flatMap { pageContent =>
      socialImageDetector.extractSocialImageUrlsFrom(pageContent)
    }

    imageDetectedImageUrls.flatMap(_.headOption).foreach { imageURL =>
      log.info("Found first social image: " + imageURL)
      checkResource match {
        case newsitem: Newsitem =>
          newsitem.twitterImage = Some(imageURL)
          Await.result(mongoRepository.saveResource(newsitem), TenSeconds)
      }
    }

    Future.successful(true)
  }

}
