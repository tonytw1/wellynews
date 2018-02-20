package nz.co.searchwellington.repositories.elasticsearch

import nz.co.searchwellington.model.Newsitem
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.junit.Assert.assertTrue
import org.junit.Test

import scala.concurrent.Await
import scala.concurrent.duration._

class ElasticSearchIT {

  val mongoRepository = new MongoRepository("mongodb://localhost:27017/wellynews")
  val elasticSearchIndexer = new ElasticSearchIndexer("localhost", 9200)

  val rebuild = new ElasticSearchIndexRebuildService(mongoRepository, elasticSearchIndexer)

  @Test
  def canCreateIndexes: Unit = {
    elasticSearchIndexer.createIndexes()
  }

  @Test
  def canIndexResources {
    rebuild.buildIndex(false)
  }

  @Test
  def canFilterByType {
    val newsitems = Await.result(elasticSearchIndexer.getResources(ResourceQuery(`type` = Some("N"))), Duration(10, SECONDS))
    assertTrue(newsitems._1.nonEmpty)
    assertTrue(newsitems._1.forall(i => Await.result(mongoRepository.getResourceById(i), Duration(1, MINUTES)).get.`type` == "N"))

    val websites = Await.result(elasticSearchIndexer.getResources(ResourceQuery(`type` = Some("W"))), Duration(10, SECONDS))
    assertTrue(websites._1.nonEmpty)
    assertTrue(websites._1.forall(i => Await.result(mongoRepository.getResourceById(i), Duration(10, SECONDS)).get.`type` == "W"))
  }

  @Test
  def canFilterByTag {
    val tag = mongoRepository.getTagByName("transport").get
    val withTag = ResourceQuery(tags = Some(Set(tag)))

    val taggedNewsitems = Await.result(elasticSearchIndexer.getResources(withTag.copy(`type` = Some("N"))), Duration(10, SECONDS))
    assertTrue(taggedNewsitems._1.nonEmpty)
    assertTrue(taggedNewsitems._1.forall(i => Await.result(mongoRepository.getResourceById(i), Duration(1, MINUTES)).get.`type` == "N"))
    assertTrue(taggedNewsitems._1.forall { i =>
      Await.result(mongoRepository.getTaggingsFor(i), Duration(1, MINUTES)).exists(t => t.tag_id == tag.id)
    })

    val taggedWebsites = Await.result(elasticSearchIndexer.getResources(withTag.copy(`type` = Some("N"))), Duration(10, SECONDS))
    assertTrue(taggedWebsites._1.nonEmpty)
    assertTrue(taggedWebsites._1.forall(i => Await.result(mongoRepository.getResourceById(i), Duration(1, MINUTES)).get.`type` == "N"))
    assertTrue(taggedWebsites._1.forall { i =>
      Await.result(mongoRepository.getTaggingsFor(i), Duration(1, MINUTES)).exists(t => t.tag_id == tag.id)
    })
  }

  @Test
  def canFilterByPublisher {
    val publisher = Await.result(mongoRepository.getWebsiteByUrlwords("wellington-city-council"), Duration(10, SECONDS)).get

    val publisherNewsitems: (Seq[Int], Long) = Await.result(elasticSearchIndexer.getResources(ResourceQuery(`type` = Some("N"), publisher = Some(publisher))), Duration(10, SECONDS))

    assertTrue(publisherNewsitems._1.nonEmpty)
    assertTrue(publisherNewsitems._1.forall(i => Await.result(mongoRepository.getResourceById(i), Duration(1, MINUTES)).get.asInstanceOf[Newsitem].getPublisher == Some(1407)))
  }

}
