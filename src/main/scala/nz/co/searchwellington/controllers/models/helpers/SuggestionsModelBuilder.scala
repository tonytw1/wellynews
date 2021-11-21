package nz.co.searchwellington.controllers.models.helpers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.controllers.RssUrlBuilder
import nz.co.searchwellington.feeds.suggesteditems.SuggestedFeeditemsService
import nz.co.searchwellington.feeds.whakaoko.WhakaokoService
import nz.co.searchwellington.filters.RequestPath
import nz.co.searchwellington.model.User
import nz.co.searchwellington.model.frontend.FrontendFeed
import nz.co.searchwellington.model.mappers.FrontendResourceMapper
import nz.co.searchwellington.repositories.ContentRetrievalService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._

@Component class SuggestionsModelBuilder @Autowired()(suggestedFeeditemsService: SuggestedFeeditemsService,
                                                      rssUrlBuilder: RssUrlBuilder,
                                                      urlBuilder: UrlBuilder,
                                                      val contentRetrievalService: ContentRetrievalService,
                                                      whakaokoService: WhakaokoService,
                                                      mongoRepository: MongoRepository,
                                                      frontendResourceMapper: FrontendResourceMapper,
                                                      commonAttributesModelBuilder: CommonAttributesModelBuilder) extends ModelBuilder
  with ReasonableWaits {

  private val MAX_SUGGESTIONS = 50

  def isValid(request: HttpServletRequest): Boolean = {
    RequestPath.getPathFrom(request).matches("^/feeds/inbox(/(rss|json))?$")
  }

  def populateContentModel(request: HttpServletRequest, loggedInUser: Option[User]): Future[Option[ModelAndView]] = {
    for {
      suggestions <- suggestedFeeditemsService.getSuggestionFeednewsitems(MAX_SUGGESTIONS, loggedInUser)
    } yield {
      val mv = new ModelAndView().
        addObject(MAIN_CONTENT, suggestions.asJava).
        addObject("heading", "Inbox").
        addObject("link", urlBuilder.fullyQualified(urlBuilder.getFeedsInboxUrl)).
        addObject("description", "Suggested news items from local feeds.")
      commonAttributesModelBuilder.setRss(mv, rssUrlBuilder.getTitleForSuggestions, rssUrlBuilder.getRssUrlForFeedSuggestions)
      Some(mv)
    }
  }

  def populateExtraModelContent(request: HttpServletRequest, mv: ModelAndView, loggedInUser: Option[User]): Future[ModelAndView] = {
    for {
      suggestOnlyFeeds <- mongoRepository.getAllFeeds
      whakaokoSubscriptions <- whakaokoService.getSubscriptions

    } yield {
      val subscriptionsById = whakaokoSubscriptions.map { subscription =>
        (subscription.id, subscription)
      }.toMap

      val feedsWithSubscriptions = suggestOnlyFeeds.flatMap { feed =>
          val mayBeSubscription = feed.whakaokoSubscription.map { subscriptionId =>
            subscriptionsById(subscriptionId)
          }
          mayBeSubscription.map { subscription =>
            Some(feed, subscription)
          }
      }.flatten

      // Override the last change field with better information from the whakaoko subscription; TODO this field is really last accepted date or something
      val x: Seq[Future[Option[FrontendFeed]]] = feedsWithSubscriptions.map { feed =>
        frontendResourceMapper.createFrontendResourceFrom(feed._1).map {
          case frontendFeed: FrontendFeed =>
            Some(frontendFeed.copy(lastChanged = feed._2.latestItemDate.map(_.toDate)))
          case _ =>
            None
        }
      }

      val y: Future[Seq[FrontendFeed]] = Future.sequence(x).map { f: Seq[Option[FrontendFeed]] =>
        val flatten: Seq[FrontendFeed] = f.flatten
        flatten.sortBy(_.latestItemDate).reverse
      }

      val sortedFeeds = Await.result(y, TenSeconds)
      mv.addObject("righthand_heading", "Suggest only feeds")
      mv.addObject("righthand_description", "Newsitems from these feeds are not automatically accepted.")
      mv.addObject("righthand_content", sortedFeeds)
    }
  }

  def getViewName(mv: ModelAndView, loggedInUser: Option[User]): String = "suggestions"

}
