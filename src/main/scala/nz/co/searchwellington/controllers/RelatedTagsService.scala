package nz.co.searchwellington.controllers

// TODO move out of controllers package
import nz.co.searchwellington.model.frontend.FrontendTag
import nz.co.searchwellington.model.{PublisherContentCount, Tag, TagContentCount, Website}
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchBackedResourceDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.geo.model.Place

@Component class RelatedTagsService @Autowired()(val elasticSearchBackedResourceDAO: ElasticSearchBackedResourceDAO, val tagDAO: TagDAO, val showBrokenDecisionService: ShowBrokenDecisionService) {

  def getRelatedLinksForTag(tag: Tag, maxItems: Int): Seq[TagContentCount] = {

    def removeUnsuitableTags(tag: Tag, tagFacetsForTag: Map[String, Integer]): Seq[TagContentCount] = {

      def isTagSuitableRelatedTag(tag: Tag, relatedTag: Tag): Boolean = {
        !(relatedTag.isHidden) && !(tag == relatedTag) && !(relatedTag.isParentOf(tag)) && !(tag.getAncestors.contains(relatedTag)) && !(tag.getChildren.contains(relatedTag)) && !(relatedTag.getName == "places") && !(relatedTag.getName == "blogs") // TODO push up
      }

      tagFacetsForTag.keys.map { tagId =>
        tagDAO.loadTagByName(tagId).flatMap { facetTag =>
          if (isTagSuitableRelatedTag(tag, facetTag)) {
            tagFacetsForTag.get(tagId).flatMap { count =>
              val frontendTag: FrontendTag = new FrontendTag(facetTag.getName, facetTag.getDisplayName)
              Some(new TagContentCount(frontendTag, count))
            }
          } else {
            None
          }
        }
      }.flatten.toSeq
    }

    val tagFacetsForTag = elasticSearchBackedResourceDAO.getTagFacetsForTag(tag)
    import scala.collection.JavaConverters._
    val filtered = removeUnsuitableTags(tag, tagFacetsForTag.asScala.toMap)
    filtered.take(5)
  }

  def getKeywordSearchFacets(keywords: String, tag: Tag): Seq[TagContentCount] = {
    Seq() // TODO implement
  }

  def getRelatedTagsForLocation(place: Place, radius: Double, maxItems: Int): Seq[TagContentCount] = {
    Seq()
  }

  def getRelatedPublishersForTag(tag: Tag, maxItems: Int): Seq[PublisherContentCount] = {
    import scala.collection.JavaConverters._
    val publisherFacetsForTag = elasticSearchBackedResourceDAO.getPublisherFacetsForTag(tag).asScala.toMap
    populatePublisherFacets(publisherFacetsForTag)
  }

  def getRelatedPublishersForLocation(place: Place, radius: Double): Seq[PublisherContentCount] = {
    import scala.collection.JavaConverters._
    val publisherFacetsNear: java.util.Map[String, Integer] = elasticSearchBackedResourceDAO.getPublisherFacetsNear(place.getLatLong, radius, showBrokenDecisionService.shouldShowBroken)
    populatePublisherFacets(publisherFacetsNear.asScala.toMap)
  }

  def getRelatedLinksForPublisher(publisher: Website): Seq[TagContentCount] = {
    Seq()
  }

  def getFeedworthyTags(shouldShowBroken: Boolean): Seq[TagContentCount] = {
    Seq()
  }

  private def populatePublisherFacets(publisherFacetsForTag: Map[String, Integer]): Seq[PublisherContentCount] = {
    publisherFacetsForTag.keys.map { publisher =>
      publisherFacetsForTag.get(publisher).map { count =>
        new PublisherContentCount(publisher, count)
      }
    }.flatten.toSeq
  }

}