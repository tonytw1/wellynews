package nz.co.searchwellington.views

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Newsitem
import nz.co.searchwellington.model.frontend.{FrontendNewsitem, FrontendResource}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Component
class DeduplicateImageService @Autowired()(mongoRepository: MongoRepository) extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[DeduplicateImageService])

  var map: Map[BSONObjectID, Map[String, Int]] = Map.empty

  {
    log.info("Creating image usage map")
    map = Await.result(indexImages(), FiveMinutes)
    log.info("Created image usge map: " + map)
  }

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
          pmap <- map.get(p)
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

  // Produce a map of image url usages grouped by publisher
  private def indexImages(): Future[Map[BSONObjectID, Map[String, Int]]] = {
    mongoRepository.getAllResourceIds().map { resourceIds =>
      val publisherImageUsages: Seq[(BSONObjectID, String)] = resourceIds.flatMap { id =>
        val maybeResource = Await.result(mongoRepository.getResourceByObjectId(id), TenSeconds)
        maybeResource.flatMap {
          case newsitem: Newsitem =>
            for {
              publisher <- newsitem.publisher
              image <- newsitem.twitterImage
            } yield {
              (publisher, image)
            }
          case _ =>
            None
        }
      }

      val groupedByPublisher = publisherImageUsages.groupBy(_._1)
      groupedByPublisher.map { p =>
        val counts = p._2.map(_._2).groupBy(identity).view.mapValues(_.size)
        (p._1, counts.toMap)
      }

    }
  }

}
