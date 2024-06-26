package nz.co.searchwellington.tagging

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.taggingvotes.{GeneratedTaggingVote, GeotaggingVote, HandTagging, TaggingVote}
import nz.co.searchwellington.repositories.HandTaggingDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{ExecutionContext, Future}

// The tagging returns officer is responsible for collecting all opinions / votes on how a resource should be tagged
// These votes are used to determine how a resource is indexed and how it renders on the frontend.
// Behaviours like a newsitem's publisher's tags should contribute to that newsitem's tagging are expressed here using tagging votes.
@Component class TaggingReturnsOfficerService @Autowired()(handTaggingDAO: HandTaggingDAO, val mongoRepository: MongoRepository)
  extends ReasonableWaits with TagAncestors {

  def getTaggingsVotesForResource(resource: Tagged)(implicit ec: ExecutionContext): Future[Seq[TaggingVote]] = {
    def eventualAncestorTagVotes(handTaggings: Seq[HandTagging]): Future[Seq[GeneratedTaggingVote]] = {
        val handTags = handTaggings.map(_.tag).distinct
        Future.sequence(handTags.map { rt =>
          parentsOf(rt).map(parents => parents.map(fat => GeneratedTaggingVote(fat, s"Ancestor of tag ${rt.name}")))
        }).map(_.flatten)
    }

    val eventualPublisherVotes = resource match {
      case p: PublishedResource =>
        val eventualGeneratePublisherDerivedTagVotes = generatePublisherDerivedTagVotes(p)
        for {
          generatePublisherDerivedTagVotes <- eventualGeneratePublisherDerivedTagVotes
        } yield {
          Seq(generatePublisherDerivedTagVotes).flatten
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
      handTaggings <- handTaggingDAO.getHandTaggingsForResource(resource)
      ancestorTagVotes <- eventualAncestorTagVotes(handTaggings)  // TODO test coverage for ancestor tag votes
      publisherVotes <- eventualPublisherVotes
      newsitemSpecificVotes <- eventualNewsitemSpecificVotes

    } yield {
      handTaggings ++ ancestorTagVotes ++ publisherVotes ++ newsitemSpecificVotes
    }
  }

  def getGeotagVotesForResource(resource: Resource)(implicit ec: ExecutionContext): Future[Seq[GeotaggingVote]] = {
    val resourceGeocodeVote = resource.geocode.filter(_.isValid).map { g =>
      GeotaggingVote(g, "Resources own geo tag", 1) // TODO resource owner as the voter
    }

    val eventualPublisherGeocodeVote = {
      (resource match {
        case pr: PublishedResource =>
          pr.publisher.map { p =>
            mongoRepository.getResourceByObjectId(p).map { maybePublisher =>
              maybePublisher.flatMap { publisher =>
                publisher.geocode.filter(_.isValid).map { pg =>
                  GeotaggingVote(pg, "Publisher's location", 1)
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

    val eventualHandTaggingGeoVotes = handTaggingDAO.getHandTaggingsForResource(resource).flatMap { handTaggingsForResource =>
      val tags = handTaggingsForResource.map(_.tag).toSet
      val tagsWithGeocodes = tags.filter(t => t.geocode.exists(_.isValid))

      // Sort these tags by depth; a finer tag like naenae-pool should beat a coarser old like naenae
      val withDepths = tagsWithGeocodes.map { tag =>
        parentsOf(tag).map { parents =>
          (tag, parents.size)
        }
      }.toSeq

      val sorted = Future.sequence(withDepths).map { tuples =>
        tuples.sortBy(_._2).reverse.map(_._1)
      }
      sorted.map { geocodeTags =>
        geocodeTags.flatMap { t =>
          t.geocode.map { tagGeocode =>
            GeotaggingVote(tagGeocode, t.display_name + " tag geocode", 1)
          }
        }
      }
    }

    for {
      publisherGeocodeVote <- eventualPublisherGeocodeVote
      handTaggingGeoVotes <- eventualHandTaggingGeoVotes
    } yield {
      Seq(resourceGeocodeVote, publisherGeocodeVote).flatten ++ handTaggingGeoVotes
    }
  }

  private def generatePublisherDerivedTagVotes(p: PublishedResource)(implicit ec: ExecutionContext): Future[Seq[GeneratedTaggingVote]] = {
    p.publisher.map { pid =>
      mongoRepository.getResourceByObjectId(pid).flatMap { maybePublisher: Option[Resource] =>
        maybePublisher.map { publisher =>
          handTaggingDAO.getHandTaggingsForResource(publisher).flatMap { handTaggings =>
            Future.sequence(handTaggings.map { publishersTagging =>
              for {
                parentTags <- parentsOf(publishersTagging.tag)
              } yield {
                val publisherAncestorTagVotes = parentTags.map(pat => GeneratedTaggingVote(pat, "Ancestor of publisher tag " + publishersTagging.tag.name))
                GeneratedTaggingVote(publishersTagging.tag, "Publisher tag") +: publisherAncestorTagVotes
              }
            }).map(_.flatten)
          }
        }.getOrElse {
          Future.successful(Seq.empty)
        }
      }
    }.getOrElse {
      Future.successful(Seq.empty)
    }
  }

  private def generateFeedRelatedTags(n: Newsitem)(implicit ec: ExecutionContext): Future[Seq[GeneratedTaggingVote]] = {
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
      mongoRepository.getResourceByObjectId(fid).flatMap { maybeFeed =>
        maybeFeed.map { feed =>
          for {
            handTaggingForFeed <- handTaggingDAO.getHandTaggingsForResource(feed)
            feedHandTags = handTaggingForFeed.map(_.tag)
            acceptedFromFeedTags <- generateAcceptedFromFeedTags(feedHandTags)
          } yield {
            acceptedFromFeedTags
          }
        }.getOrElse {
          Future.successful(Seq.empty)
        }
      }
    }.getOrElse {
      Future.successful(Seq.empty)
    }
  }

}
