package nz.co.searchwellington.filters

import java.text.{ParseException, SimpleDateFormat}
import java.util.Date
import java.util.regex.{Matcher, Pattern}
import javax.servlet.http.HttpServletRequest

import com.clutch.dates.StringToTime
import com.google.common.base.Strings
import nz.co.searchwellington.model.{Image, Resource}
import nz.co.searchwellington.repositories.{HibernateResourceDAO, TagDAO}
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Scope, ScopedProxyMode}
import org.springframework.stereotype.Component
  
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS) class AdminRequestFilter @Autowired() (resourceDAO: HibernateResourceDAO, tagDAO: TagDAO, resourceParameterFilter: ResourceParameterFilter, tagsParameterFilter: TagsParameterFilter) {

  private val log = Logger.getLogger(classOf[AdminRequestFilter])
  private val DATE_FIELD = "date"
  private val EMBARGO_DATE_FIELD = "embargo_date"
  
  def loadAttributesOntoRequest(request: HttpServletRequest) {

    log.debug("Looking for tag parameter")
    if (request.getParameter("tag") != null) {
      val tagName = request.getParameter("tag")
      tagDAO.loadTagByName(tagName).map { tag =>
        request.setAttribute("tag", tag)
      }
    }

    if (request.getParameter("item") != null) {
      val item: Integer = request.getParameter("item").toInt
      request.setAttribute("item", item)
    }
    parseTwitterIdfromRequest(request).map { twitterId =>
      request.setAttribute("twitterId", twitterId)
    }
    val image = request.getParameter("image").asInstanceOf[String]
    if (!Strings.isNullOrEmpty(image)) {
      request.setAttribute("image", new Image(image, null))
    }

    log.debug("Looking for date field")
    if (request.getParameter(DATE_FIELD) != null && !request.getParameter(DATE_FIELD).isEmpty) {
      val dateString: String = request.getParameter(DATE_FIELD).asInstanceOf[String]
      val df: SimpleDateFormat = new SimpleDateFormat("dd MMM yyyy")
      try {
        val date: Date = df.parse(dateString)
        if (date != null) {
          request.setAttribute(DATE_FIELD, new DateTime(date).toDate)
        }
      }
      catch {
        case e: ParseException => {
          log.warn("Invalid date string supplied: " + dateString)
        }
      }
    }

    log.debug("Looking for embargoed field")
    if (request.getParameter(EMBARGO_DATE_FIELD) != null && !request.getParameter(EMBARGO_DATE_FIELD).isEmpty) {
      request.setAttribute(EMBARGO_DATE_FIELD, parseEmbargoDate(request.getParameter(EMBARGO_DATE_FIELD).asInstanceOf[String]).getOrElse(null))
    }
    if (request.getParameter("publisher") != null && !(request.getParameter("publisher") == "")) {
      val publisherUrlWords: String = request.getParameter("publisher")
      resourceDAO.getPublisherByUrlWords(publisherUrlWords).map { publisher =>
        request.setAttribute("publisher", publisher)
      }
    }

    tagsParameterFilter.filter(request)
    resourceParameterFilter.filter(request)
    if (request.getParameter("feed") != null) {
      val feedParameter: String = request.getParameter("feed")
      log.debug("Loading feed by url words: " + feedParameter)
      resourceDAO.loadFeedByUrlWords(feedParameter).map { feed =>
        log.debug("Found feed: " + feed.title)
        request.setAttribute("feedAttribute", feed)
      }
    }

    if (request.getParameter("parent") != null) {
      val tagName = request.getParameter("parent")
      tagDAO.loadTagByName(tagName).map { tag =>
        log.debug("Found parent tag: " + tag.getName)
        request.setAttribute("parent_tag", tag)
      }
    }
  }

  private def parseEmbargoDate(dateString: String): Option[Date] = {

    val supportedEmbargoDateFormats = Seq(new SimpleDateFormat("dd MMM yyyy HH:mm"), new SimpleDateFormat("HH:mm"))

    val parsed = supportedEmbargoDateFormats.map { dateFormat =>
      try {
        val date = dateFormat.parse(dateString)
        if (date != null) {
          Some(date)
        } else {
          None
        }
      }
      catch {
        case e: ParseException => {
          log.warn("Supplied embargo date '" + dateString + "' did not match date format: " + dateFormat.toPattern)
          None
        }
      }
    }.flatten

    val withTextDates = parsed.headOption.fold {
      val time: Date = new StringToTime(dateString)
      Option(time)
    } { p =>
      Some(p)
    }

    withTextDates.getOrElse {
      log.warn("User supplied embargo date '" + dateString + "' could not be parsed")
    }

    withTextDates
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
        case e: Exception => {
          None
        }
      }
    } else {
      None
    }
  }

}
