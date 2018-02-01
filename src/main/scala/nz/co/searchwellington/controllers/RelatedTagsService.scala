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

    def removeUnsuitableTags(tag: Tag, tagFacetsForTag: Map[String, Int]): Seq[TagContentCount] = {

      def isTagSuitableRelatedTag(tag: Tag, relatedTag: Tag): Boolean = {
        !(relatedTag.isHidden) && !(tag == relatedTag) && !(relatedTag.isParentOf(tag)) && !(tag.getAncestors.contains(relatedTag)) && !(tag.getChildren.contains(relatedTag)) && !(relatedTag.getName == "places") && !(relatedTag.getName == "blogs") // TODO push up
      }

      tagFacetsForTag.keys.flatMap { tagId =>
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
      }.toSeq
    }

    val tagFacetsForTag = elasticSearchBackedResourceDAO.getTagFacetsForTag(tag)
    val filtered = removeUnsuitableTags(tag, tagFacetsForTag)
    filtered.take(5)
  }

  def getKeywordSearchFacets(keywords: String, tag: Tag): Seq[TagContentCount] = {
    Seq() // TODO implement
  }

  def getRelatedTagsForLocation(place: Place, radius: Double, maxItems: Int): Seq[TagContentCount] = {
    Seq()
  }

  def getRelatedPublishersForTag(tag: Tag, maxItems: Int): Seq[PublisherContentCount] = {
    val publisherFacetsForTag = elasticSearchBackedResourceDAO.getPublisherFacetsForTag(tag)
    populatePublisherFacets(publisherFacetsForTag)
  }

  def getRelatedPublishersForLocation(place: Place, radius: Double): Seq[PublisherContentCount] = {
    val publisherFacetsNear = elasticSearchBackedResourceDAO.getPublisherFacetsNear(place.getLatLong, radius, showBrokenDecisionService.shouldShowBroken)
    populatePublisherFacets(publisherFacetsNear)
  }

  def getRelatedLinksForPublisher(publisher: Website): Seq[TagContentCount] = {
    Seq()
  }

  def getFeedworthyTags(shouldShowBroken: Boolean): Seq[TagContentCount] = {
    Seq()
  }

  private def populatePublisherFacets(publisherFacetsForTag: Map[String, Int]): Seq[PublisherContentCount] = {
    publisherFacetsForTag.keys.flatMap { publisher =>
      publisherFacetsForTag.get(publisher).map { count =>
        new PublisherContentCount(publisher, count)
      }
    }.toSeq
  }

}