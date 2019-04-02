package nz.co.searchwellington.tagging

import nz.co.searchwellington.model._
import nz.co.searchwellington.model.taggingvotes.voters._
import nz.co.searchwellington.model.taggingvotes.{GeneratedTaggingVote, GeotaggingVote, TaggingVote}
import nz.co.searchwellington.repositories.HandTaggingDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, SECONDS}

@Component class TaggingReturnsOfficerService @Autowired() (handTaggingDAO: HandTaggingDAO, mongoRepository: MongoRepository) {

  private var log = Logger.getLogger(classOf[TaggingReturnsOfficerService])
  private val TenSeconds = Duration(10, SECONDS)

  // TODO These are a different responsibility to tagging votes
  def getHandTagsForResource(resource: Resource): Set[Tag] = {
    handTaggingDAO.getHandTaggingsForResource(resource).map { ht => ht.tag}.toSet
  }

  // TODO These are a different responsibility to tagging votes
  def getIndexTagsForResource(resource: Resource): Set[Tag] = {
    compileTaggingVotes(resource).toList.map(taggingVote => (taggingVote.tag)).distinct.toSet
  }

  // TODO These are a different responsibility to tagging votes
  def getIndexGeocodeForResource(resource: Resource): Geocode = {
    getGeotagVotesForResource(resource).headOption.map(_.geocode).orNull
  }

  def compileTaggingVotes(resource: Resource): Seq[TaggingVote] = {
    val votes: mutable.MutableList[TaggingVote] = mutable.MutableList.empty

    val handTaggings = handTaggingDAO.getHandTaggingsForResource(resource)
    votes ++= handTaggings

    resource match {
      case p: PublishedResource =>
        val ancestorTagVotes = getHandTagsForResource(resource).flatMap { rt =>
          parentsOf(rt).map(fat => new GeneratedTaggingVote(fat, new AncestorTagVoter()))
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

  def getGeotagVotesForResource(resource: Resource): List[GeotaggingVote] = {
    val votes: mutable.MutableList[GeotaggingVote] = mutable.MutableList.empty

    resource.geocode.map { g =>
      if (g.isValid) {
        // TODO votes += new GeotaggingVote(g, resource.getOwner, 1)
      }
    }

    if ((resource.`type` == "N") && (resource.asInstanceOf[PublishedResource]).getPublisher != null) {
      /*
      val publisher: Website = (resource.asInstanceOf[PublishedResource]).getPublisher
      if (publisher.getGeocode != null && publisher.getGeocode.isValid) {
        log.debug("Adding publisher geotag: " + publisher.getGeocode.toString)
        votes += new GeotaggingVote(publisher.getGeocode, new PublishersTagsVoter, 1)
      }
      */
    }

    // TODO val tagsWithGeocodes: List[Tag] = getIndexTagsForResource(resource).toList.filter(t => {t.getGeocode != null && t.getGeocode.isValid})
    // TODO votes ++= tagsWithGeocodes.map(t => {new GeotaggingVote(t.getGeocode, new AncestorTagVoter, 1)})

    votes.toList
  }

  private def generatePublisherDerivedTagVotes(p: PublishedResource): Seq[TaggingVote] = {
    p.publisher.map { pid =>
      handTaggingDAO.getHandTaggingsForResourceId(pid).flatMap { pt =>
        val publisherAncestorTagVotes = parentsOf(pt.tag).map(pat => new GeneratedTaggingVote(pat, new PublishersTagAncestorTagVoter))
        publisherAncestorTagVotes :+ new GeneratedTaggingVote(pt.tag, new PublishersTagsVoter)
      }
    }.getOrElse(Seq.empty)
  }

  private def generateFeedRelatedTags(n: Newsitem) = {
    def generateAcceptedFromFeedTags(feedTags: Set[Tag]): Set[TaggingVote] = {
      feedTags.flatMap { ft =>
        val feedAncestorTagVotes = parentsOf(ft).map ( fat => new GeneratedTaggingVote(fat, new FeedTagAncestorTagVoter))
        feedAncestorTagVotes :+ new GeneratedTaggingVote(ft, new FeedsTagsTagVoter)
      }
    }

    n.feed.map { fid =>
      val taggingsForFeed = handTaggingDAO.getHandTaggingsForResourceId(fid)
      val feedTags = taggingsForFeed.map(_.tag).toSet
      generateAcceptedFromFeedTags(feedTags)
    }.getOrElse(Seq.empty)
  }

  private def parentsOf(tag: Tag, soFar: Seq[Tag] = Seq.empty): Seq[Tag] = {
    tag.parent.flatMap { pid =>
      Await.result(mongoRepository.getTagByObjectId(pid), TenSeconds)
    }.map { p =>
      parentsOf(p, soFar :+ p)
    }.getOrElse {
      soFar
    }
  }

}
