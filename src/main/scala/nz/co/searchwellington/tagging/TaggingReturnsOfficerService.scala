package nz.co.searchwellington.tagging

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.taggingvotes.{GeneratedTaggingVote, GeotaggingVote, TaggingVote}
import nz.co.searchwellington.repositories.HandTaggingDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Component class TaggingReturnsOfficerService @Autowired()(handTaggingDAO: HandTaggingDAO, val mongoRepository: MongoRepository)
  extends ReasonableWaits with TagAncestors {

  private val log = Logger.getLogger(classOf[TaggingReturnsOfficerService])

  // TODO These are a different responsibility to tagging votes
  def getHandTagsForResource(resource: Tagged): Future[Seq[Tag]] = {
    handTaggingDAO.getHandTaggingsForResource(resource).map { handTaggings =>
      handTaggings.map(_.tag).distinct
    }
  }

  // TODO These are a different responsibility to tagging votes
  def getIndexTagsForResource(resource: Resource): Future[Seq[Tag]] = {
    compileTaggingVotes(resource).map { taggingVotes =>
      taggingVotes.map(_.tag).distinct
    }
  }

  def getHandTaggingsForResource(resource: Tagged): Future[Seq[taggingvotes.HandTagging]] = {
    handTaggingDAO.getHandTaggingsForResource(resource)
  }

  def getIndexTaggingsForResource(resource: Resource): Future[Seq[TaggingVote]] = {
    compileTaggingVotes(resource)
  }

  // TODO These are a different responsibility to tagging votes
  def getIndexGeocodeForResource(resource: Resource): Future[Option[Geocode]] = {
    getGeotagVotesForResource(resource).map { i =>
      i.headOption.map(_.geocode)
    }
  }

  def compileTaggingVotes(resource: Resource): Future[Seq[TaggingVote]] = {
    val eventualHandTaggings = handTaggingDAO.getHandTaggingsForResource(resource)

    val eventualPublisherVotes = resource match {
      case p: PublishedResource =>
        val eventualAncestorTagVotes: Future[Seq[GeneratedTaggingVote]] = {
          getHandTagsForResource(resource).map { taggings =>
            taggings.map { rt =>
              parentsOf(rt).map(parents => parents.map(fat => GeneratedTaggingVote(fat, "Ancestor tag of " + rt.name)))
            }
          }.flatMap(Future.sequence(_)).map(_.flatten)
        }

        val eventualGeneratePublisherDerivedTagVotes = generatePublisherDerivedTagVotes(p)

        for {
          ancestorTagVotes <- eventualAncestorTagVotes
          generatePublisherDerivedTagVotes <- eventualGeneratePublisherDerivedTagVotes
        } yield {
          Seq(ancestorTagVotes, generatePublisherDerivedTagVotes).flatten // TODO test coverage for ancestor tag votes
        }

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

  def getGeotagVotesForResource(resource: Resource): Future[Seq[GeotaggingVote]] = {
    val resourceGeocodeVote: Option[GeotaggingVote] = resource.geocode.map { g =>
      new GeotaggingVote(g, "Resources own geo tag", 1) // TODO resource owner as the voter
    }

    val eventualPublisherGeocodeVote: Future[Option[GeotaggingVote]] = {
      (resource match {
        case pr: PublishedResource =>
          pr.publisher.map { p =>
            mongoRepository.getResourceByObjectId(p).map { maybePublisher =>
              maybePublisher.flatMap { publisher =>
                publisher.geocode.map { pg =>
                  new GeotaggingVote(pg, "Publisher's location", 1)
                }
              }
            }
          }
        case _ =>
          None
      }).getOrElse {
        Future.successful(None)
      }
    }

    val eventualHandTaggingGeoVotes = getHandTaggingsForResource(resource).map { handTaggingsForResource =>
      val tags = handTaggingsForResource.map(_.tag)
      val tagsWithGeocodes = tags.filter(t => t.geocode.exists(_.isValid))
      // TODO sort these tags by depth; a finer tag like naenae-pool should beat a coarser old like naenae
      tagsWithGeocodes.flatMap { t =>
        t.geocode.map { tagGeocode =>
          GeotaggingVote(tagGeocode, t.display_name + " tag geocode", 1)
        }
      }
    }

    for {
      publisherGeocodeVote <- eventualPublisherGeocodeVote
      handTaggingGeoVotes <- eventualHandTaggingGeoVotes
    } yield {
      (Seq(resourceGeocodeVote, publisherGeocodeVote).flatten ++ handTaggingGeoVotes).filter(gv => gv.geocode.isValid)  // TODO this filter is odd
    }
  }

  private def generatePublisherDerivedTagVotes(p: PublishedResource): Future[Seq[GeneratedTaggingVote]] = {
    p.publisher.map { pid =>
      handTaggingDAO.getHandTaggingsForResourceId(pid).flatMap { handTaggings =>
        Future.sequence(handTaggings.map { publishersTagging =>
          for {
            parentTags <- parentsOf(publishersTagging.tag)
          } yield {
            val publisherAncestorTagVotes = parentTags.map(pat => GeneratedTaggingVote(pat, "Ancestor of publisher tag " + publishersTagging.tag.name))
            publisherAncestorTagVotes :+ GeneratedTaggingVote(publishersTagging.tag, "Publisher tag")
          }
        }).map(_.flatten)
      }
    }.getOrElse {
      Future.successful(Seq.empty)
    }
  }

  private def generateFeedRelatedTags(n: Newsitem): Future[Seq[GeneratedTaggingVote]] = {
    def generateAcceptedFromFeedTags(feedTags: Seq[Tag]): Future[Seq[GeneratedTaggingVote]] = {
      Future.sequence {
        feedTags.map { ft =>
          for {
            parentsOfFeedTag <- parentsOf(ft)
          } yield {
            val feedAncestorTagVotes = parentsOfFeedTag.map(fat => GeneratedTaggingVote(fat, "Ancestor of feed tag " + ft.name))
            feedAncestorTagVotes :+ GeneratedTaggingVote(ft, "Feed tag")
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

    }.getOrElse {
      Future.successful(Seq.empty)
    }
  }

}
