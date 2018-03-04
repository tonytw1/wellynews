package nz.co.searchwellington.model

import org.joda.time.DateTimeZone
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.dates.DateFormatter

@Component class UrlWordsGenerator {

  def makeUrlWordsFromName(name: String): String = {
    if (name != null) {
      val urlWords = new String(name)
      urlWords.replaceAll("\\(.*?\\)", "").trim.replaceAll(" ", "-").replaceAll("\\s", "").replaceAll("[^\\w-]", "").replaceAll("-+", "-").toLowerCase
    }
    else null
  }

  /*
  def makeUrlForFrontendNewsitem(newsitem: FrontendNewsitem): String = {
    val uri = new StringBuilder
    if (newsitem.getPublisherName != null) uri.append("/" + makeUrlWordsFromName(newsitem.getPublisherName))
    val dateFormatter = new DateFormatter(DateTimeZone.UTC)
    if (newsitem.getDate != null) {
      uri.append("/" + dateFormatter.yearMonthDayUrlStub(newsitem.getDate))
      uri.append("/" + makeUrlWordsFromName(newsitem.getName))
      return uri.toString
    }
    null
  }
  */

  def makeUrlForNewsitem(newsitem: Newsitem): Option[String] = {
    val uri = new StringBuilder

    newsitem.publisher.map { p =>
      uri.append("/" + makeUrlWordsFromName(p.toString))
    }

    newsitem.date2.map { d =>
      val dateFormatter = new DateFormatter(DateTimeZone.UTC)
      uri.append("/" + dateFormatter.yearMonthDayUrlStub(d))
      uri.append("/" + makeUrlWordsFromName(newsitem.title.getOrElse("")))
      uri.toString()
    }
  }

}
