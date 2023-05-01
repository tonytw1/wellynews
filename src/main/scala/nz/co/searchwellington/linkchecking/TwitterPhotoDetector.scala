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
    val eventualImageDetectedImageUrls = maybePageContent.map { pageContent =>
      socialImageDetector.extractSocialImageUrlsFrom(pageContent)
    }.getOrElse{
      Future.successful(None)
    }

    eventualImageDetectedImageUrls.map { imageDetectedUrls =>
      imageDetectedUrls.flatMap(_.headOption).foreach { detectedImage =>
        log.info("Found first social image: " + detectedImage)
        checkResource match {
          case newsitem: Newsitem =>
            newsitem.twitterImage = Some(detectedImage.url)
            Await.result(mongoRepository.saveResource(newsitem), TenSeconds)
          case _ =>
        }
      }
    }.map { _ =>
      true
    }
  }

}
