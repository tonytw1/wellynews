package nz.co.searchwellington.tagging

import io.opentelemetry.api.trace.Span
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model._
import nz.co.searchwellington.repositories.elasticsearch.{Circle, ElasticSearchIndexer, ResourceQuery}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.api.bson.BSONObjectID
import uk.co.eelpieconsulting.common.geo.model.{LatLong, Place}

import scala.concurrent.{ExecutionContext, Future}

@Component class RelatedTagsService @Autowired()(val mongoRepository: MongoRepository, elasticSearchIndexer: ElasticSearchIndexer) extends ReasonableWaits with TagAncestors {

  private val newsitems = Some(Set("N"))

  def getRelatedTagsForTag(tag: Tag, maxItems: Int, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Seq[TagContentCount]] = {
    def getTagAggregation(tag: Tag, loggedInUser: Option[User]): Future[Seq[(String, Long)]] = {
      val newsitemsForTag = ResourceQuery(`type` = newsitems, tags = Some(Set(tag)))
      elasticSearchIndexer.getTagAggregationFor(newsitemsForTag, loggedInUser)
    }

    val eventualTagsAncestors = parentsOf(tag)
    val eventualTagsDescendants = descendantsOf(tag)
    val eventualTagContentCounts = getTagAggregation(tag, loggedInUser).flatMap { ts =>
      Future.sequence(ts.map(toTagContentCount)).map(_.flatten)
    }

    for {
      tagsAncestors <- eventualTagsAncestors
      tagsDescendants <- eventualTagsDescendants
      tagContentContents <- eventualTagContentCounts
    } yield {

      def suitableRelatedTag(tagFacetsForTag: TagContentCount): Boolean = {
        def isTagSuitableRelatedTag(relatedTag: Tag): Boolean = {
          val isNotParentOf = !tagsAncestors.contains(relatedTag)
          val isNotDescendantOf = !tagsDescendants.contains(relatedTag)
          val isNotBlogsOrPlaces = !Set("blogs", "places").contains(relatedTag.name)

          tag != relatedTag && isNotBlogsOrPlaces && isNotParentOf && isNotDescendantOf
        }

        isTagSuitableRelatedTag(tagFacetsForTag.tag)
      }

      tagContentContents.filter(suitableRelatedTag).take(maxItems)
    }
  }

  def getRelatedPublishersForTag(tag: Tag, maxItems: Int, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Seq[PublisherContentCount]] = {

    def getPublishersForTag(tag: Tag, loggedInUser: Option[User]): Future[Seq[(String, Long)]] = {
      val newsitemsForTag = ResourceQuery(`type` = newsitems, tags = Some(Set(tag)))
      elasticSearchIndexer.getPublisherAggregationFor(newsitemsForTag, loggedInUser, size = Some(maxItems))
    }

    getPublishersForTag(tag, loggedInUser).flatMap { publisherFacetsForTag =>
      Future.sequence(publisherFacetsForTag.map { publisherFacet =>
        toPublisherContentCount(publisherFacet)
      }).map(_.flatten)
    }
  }

  def getRelatedPublishersForLocation(place: Place, radius: Double, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Seq[PublisherContentCount]] = {

    def getPublishersNear(latLong: LatLong, radius: Double, loggedInUser: Option[User]): Future[Seq[(String, Long)]] = {
      elasticSearchIndexer.getPublisherAggregationFor(nearbyNewsitemsQuery(latLong, radius), loggedInUser)
    }

    getPublishersNear(place.getLatLong, radius, loggedInUser).flatMap { publisherFacetsNear =>
      Future.sequence(publisherFacetsNear.map { publisherFacet =>
        toPublisherContentCount(publisherFacet)
      }).map(_.flatten)
    }
  }

  def getRelatedTagsForPublisher(publisher: Website, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Seq[TagContentCount]] = {

    def getPublisherTags(publisher: Website, loggedInUser: Option[User]): Future[Seq[(String, Long)]] = {
      val publishersNewsitems = ResourceQuery(`type` = newsitems, publisher = Some(publisher))
      elasticSearchIndexer.getTagAggregationFor(publishersNewsitems, loggedInUser)
    }

    getPublisherTags(publisher, loggedInUser).flatMap { ts =>
      Future.sequence(ts.map(toTagContentCount)).map(_.flatten)
    }
  }

  def getRelatedTagsForLocation(place: Place, radius: Double, loggedInUser: Option[User])(implicit ec: ExecutionContext, currentSpan: Span): Future[Seq[TagContentCount]] = {

    def getTagsNear(latLong: LatLong, radius: Double, loggedInUser: Option[User]): Future[Seq[(String, Long)]] = {
      elasticSearchIndexer.getTagAggregationFor(nearbyNewsitemsQuery(latLong, radius), loggedInUser)
    }

    getTagsNear(place.getLatLong, radius, loggedInUser).flatMap { ts =>
      Future.sequence(ts.map(toTagContentCount)).map(_.flatten)
    }
  }

  private def toPublisherContentCount(facet: (String, Long))(implicit ec: ExecutionContext): Future[Option[PublisherContentCount]] = {
    val eventualMaybePublisher = mongoRepository.getResourceByObjectId(BSONObjectID.parse(facet._1).get)
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

  private def toTagContentCount(facet: (String, Long))(implicit ec: ExecutionContext): Future[Option[TagContentCount]] = {
    mongoRepository.getTagByObjectId(BSONObjectID.parse(facet._1).get).map { to =>
      to.map { tag =>
        TagContentCount(tag, facet._2)
      }
    }
  }

  private def nearbyNewsitemsQuery(latLong: LatLong, radius: Double) = ResourceQuery(`type` = newsitems, circle = Some(Circle(latLong, radius)))

}
