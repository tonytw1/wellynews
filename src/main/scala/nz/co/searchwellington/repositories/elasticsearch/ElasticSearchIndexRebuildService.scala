package nz.co.searchwellington.repositories.elasticsearch

import com.fasterxml.jackson.core.JsonProcessingException
import nz.co.searchwellington.model.{Resource, Tag}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, MINUTES}
import scala.concurrent.{Await, Future}

@Component class ElasticSearchIndexRebuildService @Autowired()(mongoRepository: MongoRepository, elasticSearchIndexer: ElasticSearchIndexer) {

  private val log = Logger.getLogger(classOf[ElasticSearchIndexRebuildService])

  private val BATCH_COMMIT_SIZE = 1000

  @throws[JsonProcessingException]
  def buildIndex(deleteAll: Boolean): Unit = {
    val resourcesToIndex = Await.result(mongoRepository.getAllResourceIds(), Duration(1, MINUTES))
    reindexResources(resourcesToIndex)
  }

  @throws[JsonProcessingException]
  private def reindexResources(resourcesToIndex: Seq[BSONObjectID]): Unit = {
    log.info("Reindexing: " + resourcesToIndex.size + " in batches of " + BATCH_COMMIT_SIZE)
    val batches = resourcesToIndex.grouped(BATCH_COMMIT_SIZE)

    var i = 0
    batches.foreach { batch =>
      log.info("Processing batch: " + batch.size + " - " + i + " / " + resourcesToIndex.size)

      val start = DateTime.now
      log.info("Loading batch")
      val eventualResources = Future.sequence(batch.map(i => mongoRepository.getResourceByObjectId(i))).map(_.flatten)
      val eventualWithIndexTags = eventualResources.flatMap { rs =>
        log.info("Loaded batch; applying tags")
        Future.sequence(rs.map { r =>
          getIndexTagIdsFor(r).map { tagIds =>
            (r, tagIds)
          }
        })
      }

      val eventualIndexing = eventualWithIndexTags.map { rs =>
        log.info("Submitting batch for indexing")
        elasticSearchIndexer.updateMultipleContentItems(rs)
        i = i + rs.size
      }
      Await.result(eventualIndexing, Duration(1, MINUTES))
    }

    log.info("Index rebuild complete")
  }

  private def getIndexTagIdsFor(resource: Resource): Future[Set[String]] = {

    def resolveParentsFor(tag: Tag, result: Seq[Tag]): Future[Seq[Tag]] = {
      tag.parent.map { p =>
        mongoRepository.getTagByObjectId(p).flatMap { pto =>
          pto.map { pt =>
            val a = pt
            if (!result.contains(pt)) {
              resolveParentsFor(pt, result :+ pt)
            } else {
              log.warn("Tag parent loop detected")
              Future.successful(result)
            }
          }.getOrElse {
            Future.successful(result)
          }
        }
      }.getOrElse{
        Future.successful(result)
      }
    }

    val eventualTags = Future.sequence(resource.resource_tags.map { tagging =>
      mongoRepository.getTagByObjectId(tagging.tag_id)
    }).map(_.flatten)

    eventualTags.flatMap { tags =>
      Future.sequence {
        tags.map { t =>
          resolveParentsFor(t, Seq())
        }
      }.map { parents =>
        (tags ++ parents.flatten).map(t => t.id).toSet
      }
    }

  }

}
