package nz.co.searchwellington.controllers

// TODO move out of controllers package
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.model._
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.repositories.elasticsearch.{Circle, ElasticSearchIndexer, ResourceQuery}
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.tagging.TagAncestors
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.api.bson.BSONObjectID
import uk.co.eelpieconsulting.common.geo.model.{LatLong, Place}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Component class RelatedTagsService @Autowired()(tagDAO: TagDAO, showBrokenDecisionService: ShowBrokenDecisionService,
                                                 frontendResourceMapper: FrontendResourceMapper,
                                                 val mongoRepository: MongoRepository,
                                                 elasticSearchIndexer: ElasticSearchIndexer) extends ReasonableWaits with TagAncestors {

  private val newsitems = Some(Set("N"))

  def getRelatedTagsForTag(tag: Tag, maxItems: Int, loggedInUser: Option[User]): Future[Seq[TagContentCount]] = {

    val tagsAncestors = Await.result(parentsOf(tag), TenSeconds) // TODO Await
    val tagsDescendants = Await.result(descendantsOf(tag), TenSeconds) // TODO Await


    def suitableRelatedTag(tagFacetsForTag: TagContentCount): Boolean = {
      def isTagSuitableRelatedTag(relatedTag: Tag): Boolean = {
        //  !(relatedTag.isHidden) && !(tag == relatedTag) && !(relatedTag.isParentOf(tag)) && !(tag.getAncestors.contains(relatedTag)) && !(tag.getChildren.contains(relatedTag)) && !(relatedTag.getName == "places") && !(relatedTag.getName == "blogs") // TODO push up

        val isNotParentOf = !tagsAncestors.contains(relatedTag)
        val isNotDescendantOf = !tagsDescendants.contains(relatedTag)

        tag != relatedTag && isNotParentOf && isNotDescendantOf // TODO implement all
      }

      isTagSuitableRelatedTag(tagFacetsForTag.tag)
    }

    def getTagAggregation(tag: Tag, loggedInUser: Option[User]): Future[Seq[(String, Long)]] = {
      val newsitemsForTag = ResourceQuery(`type` = newsitems, tags = Some(Set(tag)))
      elasticSearchIndexer.getAggregationFor(newsitemsForTag, elasticSearchIndexer.Tags, loggedInUser)
    }

    val eventualTagContentCounts: Future[Seq[TagContentCount]] = getTagAggregation(tag, loggedInUser).flatMap { ts =>
      Future.sequence(ts.map(toTagContentCount)).map(_.flatten)
    }

    eventualTagContentCounts.map { tcs =>
      tcs.filter(suitableRelatedTag).take(maxItems)
    }
  }

  def getRelatedPublishersForTag(tag: Tag, maxItems: Int, loggedInUser: Option[User]): Future[Seq[PublisherContentCount]] = {

    def getPublishersForTag(tag: Tag, loggedInUser: Option[User]): Future[Seq[(String, Long)]] = {
      val newsitemsForTag = ResourceQuery(`type` = newsitems, tags = Some(Set(tag)))
      elasticSearchIndexer.getPublisherAggregationFor(newsitemsForTag, loggedInUser)
    }

    getPublishersForTag(tag, loggedInUser).flatMap { publisherFacetsForTag =>
      Future.sequence(publisherFacetsForTag.map { publisherFacet =>
        toPublisherContentCount(publisherFacet)
      }).map(_.flatten)
    }
  }

  def getRelatedPublishersForLocation(place: Place, radius: Double, loggedInUser: Option[User]): Future[Seq[PublisherContentCount]] = {

    def getPublishersNear(latLong: LatLong, radius: Double, loggedInUser: Option[User]): Future[Seq[(String, Long)]] = {
      elasticSearchIndexer.getPublisherAggregationFor(nearbyNewsitemsQuery(latLong, radius), loggedInUser)
    }

    getPublishersNear(place.getLatLong, radius, loggedInUser).flatMap { publisherFacetsNear =>
      Future.sequence(publisherFacetsNear.map { publisherFacet =>
        toPublisherContentCount(publisherFacet)
      }).map(_.flatten)
    }
  }

  def getRelatedTagsForPublisher(publisher: Website, loggedInUser: Option[User]): Future[Seq[TagContentCount]] = {

    def getPublisherTags(publisher: Website, loggedInUser: Option[User]): Future[Seq[(String, Long)]] = {
      val publishersNewsitems = ResourceQuery(`type` = newsitems, publisher = Some(publisher))
      elasticSearchIndexer.getAggregationFor(publishersNewsitems, elasticSearchIndexer.Tags, loggedInUser)
    }

    getPublisherTags(publisher, loggedInUser).flatMap { ts =>
      Future.sequence(ts.map(toTagContentCount)).map(_.flatten)
    }
  }

  def getRelatedTagsForLocation(place: Place, radius: Double, loggedInUser: Option[User]): Future[Seq[TagContentCount]] = {

    def getTagsNear(latLong: LatLong, radius: Double, loggedInUser: Option[User]): Future[Seq[(String, Long)]] = {
      elasticSearchIndexer.getAggregationFor(nearbyNewsitemsQuery(latLong, radius), elasticSearchIndexer.Tags, loggedInUser)
    }

    getTagsNear(place.getLatLong, radius, loggedInUser).flatMap { ts =>
      Future.sequence(ts.map(toTagContentCount)).map(_.flatten)
    }
  }

  def getFeedworthyTags(shouldShowBroken: Boolean): Seq[TagContentCount] = {
    Seq()
  }

  private def toPublisherContentCount(facet: (String, Long)): Future[Option[PublisherContentCount]] = {
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

  private def toTagContentCount(facet: (String, Long)): Future[Option[TagContentCount]] = {
    mongoRepository.getTagByObjectId(BSONObjectID.parse(facet._1).get).map { to =>
      to.map { tag =>
        TagContentCount(tag, facet._2)
      }
    }
  }

  private def nearbyNewsitemsQuery(latLong: LatLong, radius: Double) = ResourceQuery(`type` = newsitems, circle = Some(Circle(latLong, radius)))

}
