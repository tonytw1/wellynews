package nz.co.searchwellington.tagging

import nz.co.searchwellington.model.taggingvotes.voters.{FeedsTagsTagVoter, PublishersTagsVoter}
import nz.co.searchwellington.model.taggingvotes.{GeneratedTaggingVote, GeotaggingVote, TaggingVote}
import nz.co.searchwellington.model.{Geocode, PublishedResource, Resource, Tag}
import nz.co.searchwellington.repositories.HandTaggingDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.mutable
import scala.concurrent.duration.{Duration, SECONDS}

@Component class TaggingReturnsOfficerService @Autowired() (handTaggingDAO: HandTaggingDAO, mongoRepository: MongoRepository) {

  private var log: Logger = Logger.getLogger(classOf[TaggingReturnsOfficerService])
  private val TenSeconds = Duration(10, SECONDS)

  def getHandTagsForResource(resource: Resource): Set[Tag] = {
    handTaggingDAO.getHandTaggingsForResource(resource).map { ht => ht.tag}.toSet
  }

  def getIndexTagsForResource(resource: Resource): Set[Tag] = {
    compileTaggingVotes(resource).toList.map(taggingVote => (taggingVote.tag)).distinct.toSet
  }

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

    if (resource.`type` == "N") {
      //val acceptedFeed: Feed = (resource.asInstanceOf[Newsitem]).getFeed
      //if (acceptedFeed != null) {
       // votes ++= addAcceptedFromFeedTags(this.getHandTagsForResource(acceptedFeed).toSet)
     // }
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

  private def addAcceptedFromFeedTags(feedsHandTags: Set[Tag]): List[TaggingVote] = {
    val feedTagVotes: mutable.MutableList[TaggingVote] = mutable.MutableList.empty
    for (tag <- feedsHandTags) {
      feedTagVotes += new GeneratedTaggingVote(tag, new FeedsTagsTagVoter)
      // TODO feedTagVotes ++= tag.getAncestors.toList.map(t => {new GeneratedTaggingVote(t, new FeedTagAncestorTagVoter)})
    }
    feedTagVotes.toList
  }

  private def generatePublisherDerivedTagVotes(resource: Resource): List[TaggingVote] = {
    val publisherTagVotes = resource match {
      case p: PublishedResource =>
        p.publisher.map { pid =>
          handTaggingDAO.getHandTaggingsForResourceId(pid).map { pt =>
            new GeneratedTaggingVote(pt.tag, new PublishersTagsVoter)
          }
        }.getOrElse {
          Seq.empty
        }
      case _ =>
        Seq.empty
    }

    log.info("Publisher derived tags: " + publisherTagVotes)

    /*
    if ((resource.asInstanceOf[PublishedResource]).getPublisher != null) {
      val publisher: Website = (resource.asInstanceOf[PublishedResource]).getPublisher
      for (publisherTag <- this.getHandTagsForResource(publisher)) {
        publisherTagVotes += new GeneratedTaggingVote(publisherTag, new PublishersTagsVoter)
        // TODO publisherTagVotes ++= publisherTag.getAncestors.toList.map(publishersTagAncestor => (new GeneratedTaggingVote(publishersTagAncestor, new PublishersTagAncestorTagVoter)))
      }
    }
    */

    publisherTagVotes.toList
  }

  private def generateAncestorTagVotes(resource: Resource): List[TaggingVote] = {
    val ancestorTagVotes: mutable.MutableList[TaggingVote] = mutable.MutableList.empty
    for (tag <- this.getHandTagsForResource(resource)) {
      // TODO ancestorTagVotes ++= tag.getAncestors.toList.map(ancestorTag => (new GeneratedTaggingVote(ancestorTag, new AncestorTagVoter)))
    }
    ancestorTagVotes.toList
  }

}
