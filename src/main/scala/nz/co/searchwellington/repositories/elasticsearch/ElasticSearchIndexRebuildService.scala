package nz.co.searchwellington.repositories.elasticsearch

import com.fasterxml.jackson.core.JsonProcessingException
import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class ElasticSearchIndexRebuildService @Autowired()(var mongoRepository: MongoRepository, val elasticSearchIndexer: ElasticSearchIndexer) {

  private val log = Logger.getLogger(classOf[ElasticSearchIndexRebuildService])
  private val BATCH_COMMIT_SIZE = 100

  @throws[JsonProcessingException]
  def buildIndex(deleteAll: Boolean): Unit = {
    val resourcesToIndex = mongoRepository.getAllNewsitems() ++ mongoRepository.getAllWebsites() ++ mongoRepository.getAllWatchlists()
    log.info("Number of resources to reindex: " + resourcesToIndex.size)

    val withTags = resourcesToIndex.map { r =>
      val tags = mongoRepository.getTaggingsFor(r.id).map(_.tag).toSet
      (r, tags)
    }

    reindexResources(withTags)
  }

  @throws[JsonProcessingException]
  private def reindexResources(resourceToIndex: Seq[(Resource, Set[Int])]) {
    val batches = resourceToIndex.grouped(BATCH_COMMIT_SIZE)
    batches.foreach { batch =>
      println("Processing batch: " + batch.size)
      elasticSearchIndexer.updateMultipleContentItems(batch)
    }
    println("Index rebuild complete")
  }

}
