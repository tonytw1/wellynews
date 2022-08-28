package nz.co.searchwellington.repositories.elasticsearch

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.geo.LatLong
import nz.co.searchwellington.model.{Feed, Newsitem, Resource, Watchlist, Website}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.IndexTagsService
import nz.co.searchwellington.urls.UrlParser
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.{Await, ExecutionContext, Future}

@Component class ElasticSearchIndexRebuildService @Autowired()(mongoRepository: MongoRepository,
                                                               elasticSearchIndexer: ElasticSearchIndexer,
                                                               val indexTagsService: IndexTagsService,
                                                               val urlParser: UrlParser) extends IndexableResource
  with ReasonableWaits {

  private val log = LogFactory.getLog(classOf[ElasticSearchIndexRebuildService])

  private val BATCH_COMMIT_SIZE = 100

  def index(resource: Resource)(implicit ec: ExecutionContext): Future[Boolean] = {
    toIndexable(resource).map { toIndex =>
      elasticSearchIndexer.updateMultipleContentItems(Seq(toIndex))
    }.map { r =>
      r.isCompleted
    }
  }

  def reindexResources(resourcesToIndex: Seq[BSONObjectID], i: Int = 0, totalResources: Int)(implicit ec: ExecutionContext): Future[Int] = {
    val remaining = resourcesToIndex.size

    def indexBatch(batch: Seq[BSONObjectID], i: Int): Future[Int] = {
      log.info("Processing batch: " + batch.size + " - " + i + " / " + remaining)

      val eventualResources = Future.sequence(batch.map(i => mongoRepository.getResourceByObjectId(i))).map(_.flatten)
      val eventualWithIndexTags = eventualResources.flatMap { rs =>

        // Migrate latlong
        rs.foreach { r =>
          r.geocode.foreach { geocode =>
            val fix = geocode.latitude.nonEmpty && geocode.longitude.nonEmpty && geocode.latLong.isEmpty
            if (fix) {
              val latLong = LatLong(geocode.latitude.get, geocode.longitude.get)
              val fixedGeocode = geocode.copy(latLong = Some(latLong))
              val fixedResource = r match {
                case w: Website => w.copy(geocode = Some(fixedGeocode))
                case f: Feed => f.copy(geocode = Some(fixedGeocode))
                case n: Newsitem => n.copy(geocode = Some(fixedGeocode))
                case l: Watchlist => l.copy(geocode = Some(fixedGeocode))
                case _ => r
              }
              log.info("Fixed resource: " + r + " -> " + fixedResource)
              Await.result(mongoRepository.saveResource(fixedResource), TenSeconds)
            }
          }
        }

        Future.sequence(rs.map(toIndexable))
      }

      eventualWithIndexTags.flatMap { rs =>
        log.debug("Submitting batch for indexing")
        elasticSearchIndexer.updateMultipleContentItems(rs).map { r =>
          i + r.result.successes.size
        }
      }
    }

    log.info("Reindexing " + remaining + " remaining in batches of " + BATCH_COMMIT_SIZE)
    val batches = resourcesToIndex.grouped(BATCH_COMMIT_SIZE).toSeq

    batches.headOption.map { batch =>
      indexBatch(batch, i).flatMap { j =>
        val remaining = batches.tail
        if (remaining.nonEmpty) {
          reindexResources(remaining.flatten, j, totalResources)
        } else {
          Future.successful(j)
        }
      }

    }.getOrElse {
      Future.successful(i)
    }
  }

}
