package nz.co.searchwellington.views

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Newsitem
import nz.co.searchwellington.model.frontend.{FrontendNewsitem, FrontendResource}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.api.bson.BSONObjectID

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Component
class DeduplicateImageService @Autowired()(mongoRepository: MongoRepository) extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[DeduplicateImageService])

  var map: Map[BSONObjectID, mutable.Map[String, Long]] = Map.empty

  {
    log.info("Creating image usage map")
    map = Await.result(indexImages(), FiveMinutes)
    log.info("Created image usge map: " + map)
  }

  def isInteresting(item: FrontendResource): Boolean = {
    // If  a publisher has more than 3 news item images then filter out images
    // which are used more than 20% of the time
    val s = score(item)
    s > 0 && s < 0.2
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

  // Produce a map of image url usage grouped by publisher
  private def indexImages(): Future[Map[BSONObjectID, mutable.Map[String, Long]]] = {
    mongoRepository.getAllResourceIds().map { resourceIds =>
      val map: mutable.Map[BSONObjectID, mutable.Map[String, Long]] = mutable.Map.empty
      resourceIds.foreach { id =>
        val maybeResource = Await.result(mongoRepository.getResourceByObjectId(id), TenSeconds)
        maybeResource.foreach {
          case newsitem: Newsitem =>
            (for {
              publisher <- newsitem.publisher
              image <- newsitem.twitterImage
            } yield {
              (publisher, image)
            }).foreach { pi =>
              val pmap = map.getOrElse(pi._1, mutable.Map.empty[String, Long])
              val c = pmap.getOrElse(pi._2, 0L)
              pmap.put(pi._2, c + 1)
              map.put(pi._1, pmap)
            }
          case _ =>
        }
      }
      map.toMap
    }
  }

}
