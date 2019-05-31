package nz.co.searchwellington.controllers

// TODO move out of controllers package
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{PublisherContentCount, Tag, TagContentCount, Website}
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexer
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.bson.BSONObjectID
import uk.co.eelpieconsulting.common.geo.model.Place

import scala.concurrent.Await

@Component class RelatedTagsService @Autowired()(tagDAO: TagDAO, showBrokenDecisionService: ShowBrokenDecisionService,
                                                 frontendResourceMapper: FrontendResourceMapper,
                                                 mongoRepository: MongoRepository,
                                                 elasticSearchIndexer: ElasticSearchIndexer) extends ReasonableWaits {

  def getRelatedTagsForTag(tag: Tag, maxItems: Int): Seq[TagContentCount] = {

    def suitableRelatedTagContentCountsFor(tag: Tag, tagFacetsForTag: Seq[(String, Long)]): Seq[TagContentCount] = {
      def isTagSuitableRelatedTag(tag: Tag, relatedTag: Tag): Boolean = {
        //  !(relatedTag.isHidden) && !(tag == relatedTag) && !(relatedTag.isParentOf(tag)) && !(tag.getAncestors.contains(relatedTag)) && !(tag.getChildren.contains(relatedTag)) && !(relatedTag.getName == "places") && !(relatedTag.getName == "blogs") // TODO push up
        false // TODO reimplement
      }
      tagFacetsForTag.flatMap(toTagContentCount).filter(cc => isTagSuitableRelatedTag(tag, cc.tag))
    }

    val tagFacetsForTag = Await.result(elasticSearchIndexer.getTagAggregation(tag), TenSeconds)
    val filtered = suitableRelatedTagContentCountsFor(tag, tagFacetsForTag)
    filtered.take(5)
  }

  def getGeocodedTagsAggregation: Seq[TagContentCount] = {
    val tagFacetsForTag = Await.result(elasticSearchIndexer.getGeocodedTagsAggregation, TenSeconds)
    tagFacetsForTag.flatMap(toTagContentCount)
  }

  def getKeywordSearchFacets(keywords: String, tag: Tag): Seq[TagContentCount] = {
    Seq() // TODO implement
  }

  def getRelatedTagsForLocation(place: Place, radius: Double, maxItems: Int): Seq[TagContentCount] = {
    Seq() // TODO implement
  }

  def getRelatedPublishersForTag(tag: Tag, maxItems: Int): Seq[PublisherContentCount] = {
    val publisherFacetsForTag = Await.result(elasticSearchIndexer.getPublishersForTag(tag), TenSeconds)
    populatePublisherFacets(publisherFacetsForTag)
  }

  def getRelatedPublishersForLocation(place: Place, radius: Double): Seq[PublisherContentCount] = {
    val publisherFacetsNear = Await.result(elasticSearchIndexer.getPublishersNear(place.getLatLong), TenSeconds)
    populatePublisherFacets(publisherFacetsNear)
  }

  def getRelatedLinksForPublisher(publisher: Website): Seq[TagContentCount] = {
    Seq()
  }

  def getFeedworthyTags(shouldShowBroken: Boolean): Seq[TagContentCount] = {
    Seq()
  }

  private def populatePublisherFacets(publisherFacetsForTag: Seq[(String, Long)]): Seq[PublisherContentCount] = {
    publisherFacetsForTag.flatMap { a =>
      val publisherId = a._1
      val count = a._2
      Await.result(mongoRepository.getResourceByObjectId(BSONObjectID(publisherId)), TenSeconds).flatMap { resource =>
        resource match {
          case publisher: Website =>
            Some(PublisherContentCount(publisher, count))
          case _ =>
            None
        }
      }
    }
  }

  private def toTagContentCount(tagFacet: (String, Long)): Option[TagContentCount] = {
    Await.result(mongoRepository.getTagById(tagFacet._1), TenSeconds).map { tag =>
      TagContentCount(tag, tagFacet._2)
    }
  }

}
