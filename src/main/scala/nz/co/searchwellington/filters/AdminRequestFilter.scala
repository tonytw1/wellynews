package nz.co.searchwellington.filters

import com.google.common.base.Strings
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Image
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Scope, ScopedProxyMode}
import org.springframework.stereotype.Component

import java.text.{ParseException, SimpleDateFormat}
import java.util.Date
import javax.servlet.http.HttpServletRequest
import scala.concurrent.{Await, ExecutionContext}

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
class AdminRequestFilter @Autowired()(mongoRepository: MongoRepository, resourceParameterFilter: ResourceParameterFilter,
                                      tagsParameterFilter: TagsParameterFilter) extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[AdminRequestFilter])
  private val DATE_FIELD = "date"
  private val EMBARGO_DATE_FIELD = "embargo_date"

  def loadAttributesOntoRequest(request: HttpServletRequest)(implicit ec: ExecutionContext): Unit = {

    log.debug("Looking for tag parameter")
    if (request.getParameter("tag") != null) {
      val tagName = request.getParameter("tag")
      Await.result(mongoRepository.getTagByUrlWords(tagName), TenSeconds).foreach { tag =>
        request.setAttribute("tag", tag)
      }
    }

    if (request.getParameter("item") != null) {
      val item: Integer = request.getParameter("item").toInt
      request.setAttribute("item", item)
    }
    parseTwitterIdfromRequest(request).foreach { twitterId =>
      request.setAttribute("twitterId", twitterId)
    }
    val image = request.getParameter("image")
    if (!Strings.isNullOrEmpty(image)) {
      request.setAttribute("image", new Image(image, null))
    }

    log.debug("Looking for date field")
    if (request.getParameter(DATE_FIELD) != null && request.getParameter(DATE_FIELD).nonEmpty) {
      val dateString = request.getParameter(DATE_FIELD)
      val df = new SimpleDateFormat("dd MMM yyyy")
      try {
        val date: Date = df.parse(dateString)
        if (date != null) {
          request.setAttribute(DATE_FIELD, new DateTime(date).toDate)
        }
      }
      catch {
        case e: ParseException =>
          log.warn("Invalid date string supplied: " + dateString)
      }
    }

    log.debug("Looking for embargoed field")
    if (request.getParameter(EMBARGO_DATE_FIELD) != null && request.getParameter(EMBARGO_DATE_FIELD).nonEmpty) {
      // request.setAttribute(EMBARGO_DATE_FIELD, parseEmbargoDate(request.getParameter(EMBARGO_DATE_FIELD).asInstanceOf[String]).getOrElse(null))
    }
    if (request.getParameter("publisher") != null && !(request.getParameter("publisher") == "")) {
      val publisherUrlWords: String = request.getParameter("publisher")
      Await.result(mongoRepository.getWebsiteByUrlwords(publisherUrlWords), TenSeconds).foreach { publisher =>
        request.setAttribute("publisher", publisher)
      }
    }

    tagsParameterFilter.filter(request)
    resourceParameterFilter.filter(request)
    if (request.getParameter("feed") != null) {
      val feedParameter: String = request.getParameter("feed")
      log.debug("Loading feed by url words: " + feedParameter)
      Await.result(mongoRepository.getFeedByUrlwords(feedParameter), TenSeconds).foreach { feed =>
        log.debug("Found feed: " + feed)
        request.setAttribute("feedAttribute", feed)
      }
    }

    if (request.getParameter("parent") != null) {
      val tagName = request.getParameter("parent")
      Await.result(mongoRepository.getTagByUrlWords(tagName), TenSeconds).map { tag =>
        request.setAttribute("parent_tag", tag)
      }
    }
  }

  private def parseTwitterIdfromRequest(request: HttpServletRequest): Option[Long] = {
    val twitterIdParam = request.getParameter("twitterid")
    log.debug("Twitted id parameter: " + twitterIdParam)
    if (twitterIdParam != null) {
      try {
        val twitterId = twitterIdParam.toLong
        log.debug("Twitted id parsed to: " + twitterId)
        Some(twitterId)
      }
      catch {
        case e: Exception =>
          None
      }
    } else {
      None
    }
  }

}
