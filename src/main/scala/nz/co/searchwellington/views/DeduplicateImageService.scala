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

@Component
class DeduplicateImageService @Autowired()(elasticSearchIndexer: ElasticSearchIndexer) extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[DeduplicateImageService])

  private var usages: Map[BSONObjectID, Map[String, Long]] = Map.empty

  def isInteresting(item: FrontendResource): Boolean = {
    // If  a publisher has more than 3 news item images then filter out images
    // which are used more than 20% of the time
    val s = score(item)
    s >= 0 && s < 0.2
  }

  private def score(item: FrontendResource): Double = {
    (item match {
      case n: FrontendNewsitem =>
        for {
          p <- n.publisherId
          url <- Option(n.twitterImage)
          pmap <- usages.get(p)
          count <- pmap.get(url)
        } yield {
          val totalUsages = pmap.values.sum
          // Do not apply filtering until the publisher has 3 items
          if (totalUsages <= 5) {
            return 0
          } else {
            (count.toFloat / totalUsages.toFloat).toDouble
          }
        }
      case _ => None
    }).getOrElse(0L)
  }


  @Scheduled(fixedRate = 600000, initialDelay = 10000)
  def reindexImages() = {
    // Update a map of image url usages grouped by publisher
    elasticSearchIndexer.buildImageUsagesMap(loggedInUser = None).map { usages =>
      this.usages = usages
      log.info("Updated image usages: " + usages)
    }
  }

}
