package nz.co.searchwellington.repositories.elasticsearch

import com.fasterxml.jackson.core.JsonProcessingException
import nz.co.searchwellington.model.{Resource, Tag}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

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
  private def reindexResources(resourcesToIndex: Seq[Int]): Unit = {
    val batches = resourcesToIndex.grouped(BATCH_COMMIT_SIZE)

    batches.foreach { batch =>
      log.info("Processing batch: " + batch.size)

      val eventualResources = Future.sequence(batch.map(i => mongoRepository.getResourceById(i))).map(_.flatten)

      val eventualWithTags = eventualResources.flatMap { rs =>
        Future.sequence(rs.map { r =>
          getTagIdsFor(r).map { tagIds =>
            (r, tagIds)
          }
        })
      }

      val x: Future[Unit] = eventualWithTags.map { rs =>
        elasticSearchIndexer.updateMultipleContentItems(rs)
      }
      Await.result(x, Duration(1, MINUTES))
      println("Next")
    }

    println("Index rebuild complete")
  }

  private def getTagIdsFor(resource: Resource) = {
    mongoRepository.getTaggingsFor(resource.id).map { taggings =>

      val tags = taggings.map { tagging =>
        mongoRepository.getTagById(tagging.tag_id)
      }.flatten

      def resolveParentsFor(tag: Tag, result: Seq[Tag]): Seq[Tag] = {
        val parentTag = tag.parent.flatMap(p => mongoRepository.getTagById(p))
        parentTag.map { p =>
          if (!result.contains(p)) {
            resolveParentsFor(p, result :+ p)
          } else {
            log.warn("Loop detected while resolving tag parents: " + tag.id + " -> " + parentTag)
            result
          }
        }.getOrElse{
          result
        }
      }

      val withParents = tags.map { t =>
        resolveParentsFor(t, Seq())
      }.flatten

      withParents.map(t => t.id).toSet
    }
  }

}
