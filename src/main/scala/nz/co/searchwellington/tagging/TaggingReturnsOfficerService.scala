package nz.co.searchwellington.tagging

import nz.co.searchwellington.model._
import nz.co.searchwellington.model.taggingvotes.voters.{FeedTagAncestorTagVoter, FeedsTagsTagVoter, PublishersTagAncestorTagVoter, PublishersTagsVoter}
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

    val shouldAppearOnPublisherAndParentTagPages = (resource.`type` == "L") || (resource.`type` == "N") || (resource.`type` == "C") || (resource.`type` == "F")
    if (shouldAppearOnPublisherAndParentTagPages) {
      votes ++= generateAncestorTagVotes(resource)
      votes ++= generatePublisherDerivedTagVotes(resource)
    }

    resource match {
      case n: Newsitem =>
        n.feed.map { fid =>
          val taggingsForFeed = handTaggingDAO.getHandTaggingsForResourceId(fid)
          votes ++= taggingsForFeed.map { tv =>
           GeneratedTaggingVote(tv.tag, new FeedsTagsTagVoter())  // TODO Test coverage 2nd argument looks like it should be an emun
          }
        }
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

  private def addAcceptedFromFeedTags(feedsHandTags: Set[Tag]): Set[TaggingVote] = {
    feedsHandTags.flatMap { ft =>
      val feedAncestorTagVotes = parentsOf(ft).map ( fat => new GeneratedTaggingVote(fat, new FeedTagAncestorTagVoter)) // TODO test coverage
      feedAncestorTagVotes :+ new GeneratedTaggingVote(ft, new FeedsTagsTagVoter)
    }
  }

  private def generatePublisherDerivedTagVotes(resource: Resource): Seq[TaggingVote] = {
    val publisherTagVotes = resource match {
      case p: PublishedResource =>
        p.publisher.map { pid =>
          handTaggingDAO.getHandTaggingsForResourceId(pid).flatMap { pt =>
            val publisherAncestorTagVotes = parentsOf(pt.tag).map ( pat => new GeneratedTaggingVote(pat, new PublishersTagAncestorTagVoter))
            publisherAncestorTagVotes :+ new GeneratedTaggingVote(pt.tag, new PublishersTagsVoter)
          }
        }
      case _ =>
        None
    }
    publisherTagVotes.getOrElse(Seq.empty)
  }

  private def generateAncestorTagVotes(resource: Resource): List[TaggingVote] = {
    val ancestorTagVotes: mutable.MutableList[TaggingVote] = mutable.MutableList.empty
    for (tag <- this.getHandTagsForResource(resource)) {
      // TODO ancestorTagVotes ++= tag.getAncestors.toList.map(ancestorTag => (new GeneratedTaggingVote(ancestorTag, new AncestorTagVoter)))
    }
    ancestorTagVotes.toList
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
