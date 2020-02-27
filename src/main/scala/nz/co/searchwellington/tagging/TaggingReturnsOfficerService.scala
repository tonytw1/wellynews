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

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

@Component class TaggingReturnsOfficerService @Autowired() (handTaggingDAO: HandTaggingDAO, mongoRepository: MongoRepository)
  extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[TaggingReturnsOfficerService])

  // TODO These are a different responsibility to tagging votes
  def getHandTagsForResource(resource: Tagged): Seq[Tag] = {
    Await.result(handTaggingDAO.getHandTaggingsForResource(resource), TenSeconds).map(_.tag)
  }

  // TODO These are a different responsibility to tagging votes
  def getIndexTagsForResource(resource: Resource): Seq[Tag] = {
    val eventualTaggingVotes = compileTaggingVotes(resource)
    Await.result(eventualTaggingVotes, TenSeconds).map(taggingVote => taggingVote.tag).distinct
  }

  // TODO These are a different responsibility to tagging votes
  def getIndexGeocodeForResource(resource: Resource): Option[Geocode] = {
    getGeotagVotesForResource(resource).headOption.map(_.geocode)
  }

  def compileTaggingVotes(resource: Resource): Future[Seq[TaggingVote]] = {
    val eventualHandTaggings = handTaggingDAO.getHandTaggingsForResource(resource)

    val eventualPublisherVotes = resource match {
      case p: PublishedResource =>
        val ancestorTagVotes = Future.sequence {
          getHandTagsForResource(resource).map { rt =>
            parentsOf(rt).map(parents => parents.map(fat => GeneratedTaggingVote(fat, new AncestorTagVoter())))
          }
        }.map(_.flatten)
        Future.sequence(Seq(ancestorTagVotes, Future.successful(generatePublisherDerivedTagVotes(p)))).map(_.flatten) // TODO test coverage for ancestor tag votes

      case _ =>
        Future.successful(Seq.empty)
    }

    val eventualNewsitemSpecificVotes = resource match {
      case n: Newsitem =>
        generateFeedRelatedTags(n)
      case _ =>
        Future.successful(Seq.empty)
    }

    for {
      handTaggings <- eventualHandTaggings
      publisherVotes <- eventualPublisherVotes
      newsitemSpecificVotes <- eventualNewsitemSpecificVotes
    } yield {
      handTaggings ++ publisherVotes ++ newsitemSpecificVotes
    }
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
      Await.result(handTaggingDAO.getHandTaggingsForResourceId(pid), TenSeconds).flatMap { pt =>
        val publisherAncestorTagVotes = Await.result(parentsOf(pt.tag), TenSeconds).map(pat => new GeneratedTaggingVote(pat, new PublishersTagAncestorTagVoter))
        publisherAncestorTagVotes :+ new GeneratedTaggingVote(pt.tag, new PublishersTagsVoter)
      }
    }.getOrElse(Seq.empty)
  }

  private def generateFeedRelatedTags(n: Newsitem): Future[Seq[GeneratedTaggingVote]] = {
    def generateAcceptedFromFeedTags(feedTags: Seq[Tag]): Future[Seq[GeneratedTaggingVote]] = {
      Future.sequence{
        feedTags.map { ft =>
          for {
            parentsOfFeedTag <- parentsOf(ft)
          } yield {
            val feedAncestorTagVotes = parentsOfFeedTag.map(fat => GeneratedTaggingVote(fat, new FeedTagAncestorTagVoter))
            feedAncestorTagVotes :+ GeneratedTaggingVote(ft, new FeedsTagsTagVoter)
          }
        }
      }
    }.map(_.flatten)

    n.feed.map { fid =>
      for {
        handTaggingForFeed <- handTaggingDAO.getHandTaggingsForResourceId(fid)
        feedHandTags = handTaggingForFeed.map(_.tag)
        acceptedFromFeedTags <- generateAcceptedFromFeedTags(feedHandTags)
      } yield {
        acceptedFromFeedTags
      }

    }.getOrElse{
      Future.successful(Seq.empty)
    }
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
