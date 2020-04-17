package nz.co.searchwellington.controllers

// TODO move out of controllers package
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model._
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexer
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.bson.BSONObjectID
import uk.co.eelpieconsulting.common.geo.model.Place

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class RelatedTagsService @Autowired()(tagDAO: TagDAO, showBrokenDecisionService: ShowBrokenDecisionService,
                                                 frontendResourceMapper: FrontendResourceMapper,
                                                 mongoRepository: MongoRepository,
                                                 elasticSearchIndexer: ElasticSearchIndexer) extends ReasonableWaits {

  def getRelatedTagsForTag(tag: Tag, maxItems: Int, loggedInUser: Option[User]): Future[Seq[TagContentCount]] = {

    def suitableRelatedTag(tagFacetsForTag: TagContentCount): Boolean = {
      def isTagSuitableRelatedTag(relatedTag: Tag): Boolean = {
        //  !(relatedTag.isHidden) && !(tag == relatedTag) && !(relatedTag.isParentOf(tag)) && !(tag.getAncestors.contains(relatedTag)) && !(tag.getChildren.contains(relatedTag)) && !(relatedTag.getName == "places") && !(relatedTag.getName == "blogs") // TODO push up

        val isNotParentOf = !tag.parent.contains(relatedTag._id)

        tag != relatedTag && isNotParentOf// TODO implement all
      }

      isTagSuitableRelatedTag(tagFacetsForTag.tag)
    }

    val eventualTagContentCounts: Future[Seq[TagContentCount]] = elasticSearchIndexer.getTagAggregation(tag, loggedInUser).flatMap { ts =>
      Future.sequence(ts.map(toTagContentCount)).map(_.flatten)
    }

    eventualTagContentCounts.map { tcs =>
      tcs.filter(suitableRelatedTag).take(maxItems)
    }
  }

  def getKeywordSearchFacets(keywords: String, tag: Tag): Seq[TagContentCount] = {
    Seq() // TODO implement
  }

  def getRelatedPublishersForTag(tag: Tag, maxItems: Int, loggedInUser: Option[User]): Future[Seq[PublisherContentCount]] = {
    elasticSearchIndexer.getPublishersForTag(tag, loggedInUser).flatMap { publisherFacetsForTag =>
      Future.sequence(publisherFacetsForTag.map { publisherFacet =>
        toPublisherContentCount(publisherFacet)
      }).map(_.flatten)
    }
  }

  def getRelatedPublishersForLocation(place: Place, radius: Double, loggedInUser: Option[User]): Future[Seq[PublisherContentCount]] = {
    elasticSearchIndexer.getPublishersNear(place.getLatLong, radius, loggedInUser).flatMap { publisherFacetsNear =>
      Future.sequence(publisherFacetsNear.map { publisherFacet =>
        toPublisherContentCount(publisherFacet)
      }).map(_.flatten)
    }
  }

  def getRelatedTagsForPublisher(publisher: Website, loggedInUser: Option[User]): Future[Seq[TagContentCount]] = {
    elasticSearchIndexer.getPublisherTags(publisher, loggedInUser).flatMap { ts =>
      Future.sequence(ts.map(toTagContentCount)).map(_.flatten)
    }
  }

  def getRelatedTagsForLocation(place: Place, radius: Double, loggedInUser: Option[User]): Future[Seq[TagContentCount]] = {
    elasticSearchIndexer.getTagsNear(place.getLatLong, radius, loggedInUser).flatMap { ts =>
      Future.sequence(ts.map(toTagContentCount)).map(_.flatten)
    }
  }

  def getFeedworthyTags(shouldShowBroken: Boolean): Seq[TagContentCount] = {
    Seq()
  }

  private def toPublisherContentCount(facet: (String, Long)): Future[Option[PublisherContentCount]] = {
    val eventualMaybePublisher = mongoRepository.getResourceByObjectId(BSONObjectID(facet._1))
    eventualMaybePublisher.map { maybePublisher =>
      maybePublisher.flatMap { resource =>
        resource match {
          case publisher: Website =>
            Some(PublisherContentCount(publisher, facet._2))
          case _ =>
            None
        }
      }
    }
  }

  private def toTagContentCount(facet: (String, Long)): Future[Option[TagContentCount]] = {
    mongoRepository.getTagByObjectId(BSONObjectID(facet._1)).map { to =>
      to.map { tag =>
        TagContentCount(tag, facet._2)
      }
    }
  }

}
