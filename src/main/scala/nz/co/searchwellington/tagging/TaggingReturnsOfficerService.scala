package nz.co.searchwellington.tagging

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.taggingvotes.voters._
import nz.co.searchwellington.model.taggingvotes.{GeneratedTaggingVote, GeotaggingVote, TaggingVote}
import nz.co.searchwellington.repositories.HandTaggingDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.mutable
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

@Component class TaggingReturnsOfficerService @Autowired() (handTaggingDAO: HandTaggingDAO, mongoRepository: MongoRepository)
  extends ReasonableWaits {

  private var log = Logger.getLogger(classOf[TaggingReturnsOfficerService])

  // TODO These are a different responsibility to tagging votes
  def getHandTagsForResource(resource: Resource): Set[Tag] = {
    handTaggingDAO.getHandTaggingsForResource(resource).map { ht => ht.tag}.toSet
  }

  // TODO These are a different responsibility to tagging votes
  def getIndexTagsForResource(resource: Resource): Set[Tag] = {
    compileTaggingVotes(resource).toList.map(taggingVote => (taggingVote.tag)).distinct.toSet
  }

  // TODO These are a different responsibility to tagging votes
  def getIndexGeocodeForResource(resource: Resource): Option[Geocode] = {
    getGeotagVotesForResource(resource).headOption.map(_.geocode)
  }

  def compileTaggingVotes(resource: Resource): Seq[TaggingVote] = {
    val votes: mutable.MutableList[TaggingVote] = mutable.MutableList.empty

    val handTaggings = handTaggingDAO.getHandTaggingsForResource(resource)
    votes ++= handTaggings

    resource match {
      case p: PublishedResource =>
        val ancestorTagVotes = getHandTagsForResource(resource).flatMap { rt =>
          Await.result(parentsOf(rt), TenSeconds).map(fat => new GeneratedTaggingVote(fat, new AncestorTagVoter()))
        }
        votes ++= ancestorTagVotes  // TODO test coverage
        votes ++= generatePublisherDerivedTagVotes(p)
      case _ =>
    }

    resource match {
      case n: Newsitem =>
        votes ++= generateFeedRelatedTags(n)
      case _ =>
    }

    votes
  }

  def getGeotagVotesForResource(resource: Resource): Seq[GeotaggingVote] = {

    val resourceGeocodeVote: Option[GeotaggingVote] = resource.geocode.map { g =>
      new GeotaggingVote(g, new PublishersTagsVoter, 1) // TODO resource owner as the voter
    }

    val publisherGeocodeVote = resource match {
      case pr: PublishedResource =>
        pr.publisher.flatMap { p =>
          Await.result(mongoRepository.getResourceByObjectId(p), TenSeconds).flatMap { publisher =>
            publisher.geocode.map { pg =>
              log.debug("Adding publisher geotag: " + pg)
              new GeotaggingVote(pg, new PublishersTagsVoter, 1)
            }
          }
        }
      case _ =>
        None
    }

    // TODO val tagsWithGeocodes: List[Tag] = getIndexTagsForResource(resource).toList.filter(t => {t.getGeocode != null && t.getGeocode.isValid})
    // TODO votes ++= tagsWithGeocodes.map(t => {new GeotaggingVote(t.getGeocode, new AncestorTagVoter, 1)})

    Seq(resourceGeocodeVote, publisherGeocodeVote).flatten.filter(gv => gv.geocode.isValid)
  }

  private def generatePublisherDerivedTagVotes(p: PublishedResource): Seq[TaggingVote] = {
    p.publisher.map { pid =>
      handTaggingDAO.getHandTaggingsForResourceId(pid).flatMap { pt =>
        val publisherAncestorTagVotes = Await.result(parentsOf(pt.tag), TenSeconds).map(pat => new GeneratedTaggingVote(pat, new PublishersTagAncestorTagVoter))
        publisherAncestorTagVotes :+ new GeneratedTaggingVote(pt.tag, new PublishersTagsVoter)
      }
    }.getOrElse(Seq.empty)
  }

  private def generateFeedRelatedTags(n: Newsitem) = {
    def generateAcceptedFromFeedTags(feedTags: Set[Tag]): Set[TaggingVote] = {
      feedTags.flatMap { ft =>
        val feedAncestorTagVotes = Await.result(parentsOf(ft), TenSeconds).map ( fat => new GeneratedTaggingVote(fat, new FeedTagAncestorTagVoter))
        feedAncestorTagVotes :+ new GeneratedTaggingVote(ft, new FeedsTagsTagVoter)
      }
    }

    n.feed.map { fid =>
      val taggingsForFeed = handTaggingDAO.getHandTaggingsForResourceId(fid)
      val feedTags = taggingsForFeed.map(_.tag).toSet
      generateAcceptedFromFeedTags(feedTags)
    }.getOrElse(Seq.empty)
  }

  private def parentsOf(tag: Tag, soFar: Seq[Tag] = Seq.empty): Future[Seq[Tag]] = {
    tag.parent.map { pid =>
      mongoRepository.getTagByObjectId(pid).flatMap { pto =>
        pto.map { p =>
          parentsOf(p, soFar :+ p)
        }.getOrElse {
          Future.successful(soFar)
        }
      }
    }.getOrElse {
      Future.successful(soFar)
    }
  }

}
