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

  def getHandTagsForResource(resource: Resource): java.util.Set[Tag] = {  // TODO no real value added by this method?
    val tags: scala.collection.mutable.Set[Tag] = scala.collection.mutable.Set.empty

    val handTaggings: java.util.List[HandTagging] = handTaggingDAO.getHandTaggingsForResource(resource)
    for (tagging <- handTaggings) {
      tags.add(tagging.getTag)  // TODO map this
    }

    return tags
  }

  def getIndexTagsForResource(resource: Resource): java.util.Set[Tag] = {
    val indexTags: mutable.Set[Tag] = mutable.Set.empty
    for (vote <- compileTaggingVotes(resource)) {
      indexTags.add(vote.getTag);
    }
    return indexTags
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

  def compileTaggingVotes(resource: Resource): List[TaggingVote] = {
    val votes: mutable.MutableList[TaggingVote] = mutable.MutableList.empty
    for (handTagging <- handTaggingDAO.getHandTaggingsForResource(resource)) {
      votes += handTagging
    }

    val shouldAppearOnPublisherAndParentTagPages: Boolean = (resource.getType == "L") || (resource.getType == "N") || (resource.getType == "C") || (resource.getType == "F")
    if (shouldAppearOnPublisherAndParentTagPages) {
      addAncestorTagVotes(resource, votes)
      addPublisherDerviedTags(resource, votes)
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

  private def addPublisherDerviedTags(resource: Resource, votes: mutable.MutableList[TaggingVote]) {
    if ((resource.asInstanceOf[PublishedResource]).getPublisher != null) {
      val publisher: Website = (resource.asInstanceOf[PublishedResource]).getPublisher
      for (publisherTag <- this.getHandTagsForResource(publisher)) {
        votes += new GeneratedTaggingVote(publisherTag, new PublishersTagsVoter)
        for (publishersAncestor <- publisherTag.getAncestors) {
          votes += new GeneratedTaggingVote(publishersAncestor, new PublishersTagAncestorTagVoter)
        }
      }
    }
  }

  private def addAncestorTagVotes(resource: Resource, votes: mutable.MutableList[TaggingVote]) {  // TODO Not very functional
    for (tag <- this.getHandTagsForResource(resource)) {
      for (ancestorTag <- tag.getAncestors) {
        votes += new GeneratedTaggingVote(ancestorTag, new AncestorTagVoter)
      }
    }
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