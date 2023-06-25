package nz.co.searchwellington.views

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.frontend.{FrontendNewsitem, FrontendResource}
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexer
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component
class DeduplicateImageService @Autowired()(elasticSearchIndexer: ElasticSearchIndexer) extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[DeduplicateImageService])

  private var usages: Map[BSONObjectID, Map[String, Long]] = Map.empty

  def isInteresting(item: FrontendResource): Boolean = {
    val mayBeCardImage = item match {
      case n: FrontendNewsitem => for {
        publisher <- n.publisherId
        cardImage <- n.twitterImage
      } yield {
        (publisher, cardImage)
      }
      case _ => None
    }

    mayBeCardImage.exists { pi =>
      // Filter out images which this publisher has used more than 20% of the time.
      val s = score(pi)
      s < 0.2
    }
  }

  private def score(pi: (BSONObjectID, String)): Double = {
    (for {
      pmap <- usages.get(pi._1)
      count <- pmap.get(pi._2)
    } yield {
      val totalUsages = pmap.values.sum
      if (totalUsages <= 5) {
        return 0
      } else {
        (count.toFloat / totalUsages.toFloat).toDouble
      }
    }).getOrElse(0.0)
  }

  @Scheduled(fixedRate = 600000, initialDelay = 10000)
  def reindexImages(): Future[Unit] = {
    // Update a map of image url usages grouped by publisher
    elasticSearchIndexer.buildImageUsagesMap(loggedInUser = None).map { usages =>
      this.usages = usages
      log.info("Updated image usages: " + usages.size)
    }
  }

}
