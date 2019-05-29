package nz.co.searchwellington.controllers

// TODO move out of controllers package
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model.{PublisherContentCount, Tag, TagContentCount, Website}
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.repositories.elasticsearch.{ElasticSearchBackedResourceDAO, ElasticSearchIndexer}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.geo.model.Place

import scala.concurrent.Await

@Component class RelatedTagsService @Autowired()(elasticSearchBackedResourceDAO: ElasticSearchBackedResourceDAO,
                                                 tagDAO: TagDAO, showBrokenDecisionService: ShowBrokenDecisionService,
                                                 frontendResourceMapper: FrontendResourceMapper,
                                                 mongoRepository: MongoRepository,
                                                 elasticSearchIndexer: ElasticSearchIndexer) extends ReasonableWaits {

  def getRelatedTagsForTag(tag: Tag, maxItems: Int): Seq[TagContentCount] = {

    def removeUnsuitableTags(tag: Tag, tagFacetsForTag: Map[String, Long]): Seq[TagContentCount] = {

      def isTagSuitableRelatedTag(tag: Tag, relatedTag: Tag): Boolean = {
        //  !(relatedTag.isHidden) && !(tag == relatedTag) && !(relatedTag.isParentOf(tag)) && !(tag.getAncestors.contains(relatedTag)) && !(tag.getChildren.contains(relatedTag)) && !(relatedTag.getName == "places") && !(relatedTag.getName == "blogs") // TODO push up
        false // TODO reimplement
      }

      tagFacetsForTag.keys.flatMap { tagId =>
        Await.result(mongoRepository.getTagById(tagId), TenSeconds).flatMap { facetTag =>
          if (isTagSuitableRelatedTag(tag, facetTag)) {
            tagFacetsForTag.get(tagId).flatMap { count =>
              Some(new TagContentCount(frontendResourceMapper.mapTagToFrontendTag(facetTag), count))
            }
          } else {
            None
          }
        }
      }.toSeq
    }

    val tagFacetsForTag = Await.result(elasticSearchIndexer.getTagAggregation(tag), TenSeconds)
    val filtered = removeUnsuitableTags(tag, tagFacetsForTag.toMap)
    filtered.take(5)
  }

  def getKeywordSearchFacets(keywords: String, tag: Tag): Seq[TagContentCount] = {
    Seq() // TODO implement
  }

  def getRelatedTagsForLocation(place: Place, radius: Double, maxItems: Int): Seq[TagContentCount] = {
    Seq() // TODO implement
  }

  def getRelatedPublishersForTag(tag: Tag, maxItems: Int): Seq[PublisherContentCount] = {
    val publisherFacetsForTag = Await.result(elasticSearchIndexer.getPublishersForTag(tag), TenSeconds)
    populatePublisherFacets(publisherFacetsForTag.toMap)
  }

  def getRelatedPublishersForLocation(place: Place, radius: Double): Seq[PublisherContentCount] = {
    val publisherFacetsNear = Await.result(elasticSearchIndexer.getPublishersNear(place.getLatLong), TenSeconds)
    populatePublisherFacets(publisherFacetsNear.toMap)
  }

  def getRelatedLinksForPublisher(publisher: Website): Seq[TagContentCount] = {
    Seq()
  }

  def getFeedworthyTags(shouldShowBroken: Boolean): Seq[TagContentCount] = {
    Seq()
  }

  private def populatePublisherFacets(publisherFacetsForTag: Map[String, Long]): Seq[PublisherContentCount] = {
    publisherFacetsForTag.keys.flatMap { publisher =>
      publisherFacetsForTag.get(publisher).map { count =>
        new PublisherContentCount(publisher, count)
      }
    }.toSeq
  }

}
