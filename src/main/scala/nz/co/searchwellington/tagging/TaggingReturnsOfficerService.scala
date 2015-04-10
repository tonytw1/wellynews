package nz.co.searchwellington.tagging

import java.util

import com.google.common.collect.{Lists, Sets}
import nz.co.searchwellington.model.{Feed, Geocode, Newsitem, PublishedResource, Resource, Tag, Website}
import nz.co.searchwellington.model.taggingvotes.{GeneratedTaggingVote, GeotaggingVote, HandTagging, TaggingVote}
import nz.co.searchwellington.model.taggingvotes.voters.{AncestorTagVoter, FeedTagAncestorTagVoter, FeedsTagsTagVoter, PublishersTagAncestorTagVoter, PublishersTagsVoter}
import nz.co.searchwellington.repositories.HandTaggingDAO
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._
import scala.collection.mutable

@Component class TaggingReturnsOfficerService @Autowired() (handTaggingDAO: HandTaggingDAO) {

  private var log: Logger = Logger.getLogger(classOf[TaggingReturnsOfficerService])

  def getHandTagsForResource(resource: Resource): java.util.Set[Tag] = {
    val toSet = handTaggingDAO.getHandTaggingsForResource(resource).toList.map(handTagging => (handTagging.getTag)).distinct.toSet
    return toSet
  }

  def getIndexTagsForResource(resource: Resource): java.util.Set[Tag] = {
    val toSet = compileTaggingVotes(resource).toList.map(taggingVote => (taggingVote.getTag)).distinct.toSet
    return toSet
  }

  def getIndexGeocodeForResource(resource: Resource): Geocode = {
    val votes: List[GeotaggingVote] = getGeotagVotesForResource(resource).toList
    if (!votes.isEmpty) {
      return votes.get(0).getGeotag
    }
    return null
  }

  def getGeotagVotesForResource(resource: Resource): java.util.List[GeotaggingVote] = {
    val votes: mutable.MutableList[GeotaggingVote] = mutable.MutableList.empty
    if (resource.getGeocode != null && resource.getGeocode.isValid) {
      votes += new GeotaggingVote(resource.getGeocode, resource.getOwner, 1)
    }
    if ((resource.getType == "N") && (resource.asInstanceOf[PublishedResource]).getPublisher != null) {
      val publisher: Website = (resource.asInstanceOf[PublishedResource]).getPublisher
      if (publisher.getGeocode != null && publisher.getGeocode.isValid) {
        log.debug("Adding publisher geotag: " + publisher.getGeocode.toString)
        votes += new GeotaggingVote(publisher.getGeocode, new PublishersTagsVoter, 1)
      }
    }
    val tagGeocode: Geocode = getGeotagFromFirstResourceTagWithLocation(getIndexTagsForResource(resource).toSet)  // TODO should take them all and let someone else decide?
      if (tagGeocode != null && tagGeocode.isValid) {
      votes += new GeotaggingVote(tagGeocode, new AncestorTagVoter, 1)
    }
    return votes
  }

  def compileTaggingVotes(resource: Resource): java.util.List[TaggingVote] = {
    val votes: mutable.MutableList[TaggingVote] = mutable.MutableList.empty

    val handTaggings: List[HandTagging] = handTaggingDAO.getHandTaggingsForResource(resource).toList
    votes ++= handTaggings;

    val shouldAppearOnPublisherAndParentTagPages: Boolean = (resource.getType == "L") || (resource.getType == "N") || (resource.getType == "C") || (resource.getType == "F")
    if (shouldAppearOnPublisherAndParentTagPages) {
      votes ++= generateAncestorTagVotes(resource)
      votes ++= generatePublisherDerivedTagVotes(resource)
    }
    
    if (resource.getType == "N") {
      val acceptedFeed: Feed = (resource.asInstanceOf[Newsitem]).getFeed
      if (acceptedFeed != null) {
        val handTags: util.Set[Tag] = this.getHandTagsForResource(acceptedFeed)
        addAcceptedFromFeedTags(resource, handTags.toSet, votes)
      }
    }
    return votes.toList
  }

  private def addAcceptedFromFeedTags(resource: Resource, feedsHandTags: Set[Tag], votes: mutable.MutableList[TaggingVote]) {
    for (tag <- feedsHandTags) {
      votes += new GeneratedTaggingVote(tag, new FeedsTagsTagVoter)
      import scala.collection.JavaConversions._
      for (feedTagAncestor <- tag.getAncestors) {
        votes += new GeneratedTaggingVote(feedTagAncestor, new FeedTagAncestorTagVoter)
      }
    }
  }

  private def generatePublisherDerivedTagVotes(resource: Resource): mutable.MutableList[TaggingVote] = {
    val publisherTagVotes: mutable.MutableList[TaggingVote] = mutable.MutableList.empty

    if ((resource.asInstanceOf[PublishedResource]).getPublisher != null) {
      val publisher: Website = (resource.asInstanceOf[PublishedResource]).getPublisher
      for (publisherTag <- this.getHandTagsForResource(publisher)) {
        publisherTagVotes += new GeneratedTaggingVote(publisherTag, new PublishersTagsVoter)
        publisherTagVotes ++= publisherTag.getAncestors.toList.map(publishersTagAncestor => (new GeneratedTaggingVote(publishersTagAncestor, new PublishersTagAncestorTagVoter)))
      }
    }
    publisherTagVotes;
  }

  private def generateAncestorTagVotes(resource: Resource): mutable.MutableList[TaggingVote] = {
    val ancestorTagVotes: mutable.MutableList[TaggingVote] = mutable.MutableList.empty
    for (tag <- this.getHandTagsForResource(resource)) {
      ancestorTagVotes ++= tag.getAncestors.toList.map(ancestorTag => (new GeneratedTaggingVote(ancestorTag, new AncestorTagVoter)))
    }
    ancestorTagVotes
  }

  private def getGeotagFromFirstResourceTagWithLocation(indexTagsForResource: Set[Tag]): Geocode = {
    for (tag <- indexTagsForResource) {
      if (tag.getGeocode != null && tag.getGeocode.isValid) {
        log.debug("Found subsitute geotag for resource on resource index tag: " + tag.getName)
        return tag.getGeocode
      }
    }
    return null
  }

}