package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.feeds.reading.WhakaokoService
import nz.co.searchwellington.model.{Feed, UrlWordsGenerator}
import nz.co.searchwellington.modification.ContentUpdateService
import nz.co.searchwellington.repositories.mongo.MongoRepository
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod}
import org.springframework.web.servlet.ModelAndView

import scala.concurrent.ExecutionContext.Implicits.global

@Controller
class AcceptFeedItemController @Autowired()(contentUpdateService: ContentUpdateService,
                                            mongoRepository: MongoRepository,
                                            urlWordsGenerator: UrlWordsGenerator, urlBuilder: UrlBuilder,
                                            whakaokoService: WhakaokoService,
                                            loggedInUserFilter: LoggedInUserFilter) extends ReasonableWaits with AcceptancePolicyOptions {

  private val log = Logger.getLogger(classOf[AcceptFeedItemController])

  /*
  @RequestMapping(value = Array("/accept-feed-item"), method = Array(RequestMethod.GET))
  def accept(feed: String, url: String): ModelAndView = {


    val eventualMaybeResource = mongoRepository.getResourceById(feed)

    eventualMaybeResource.map { fo =>
      fo.map { f =>
        f match {
          case feed: Feed => {
            None



          }
          case _ =>
            None
        }

      }

   */
  
}