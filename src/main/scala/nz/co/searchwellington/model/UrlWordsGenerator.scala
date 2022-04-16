package nz.co.searchwellington.model

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.dates.DateFormatter

@Component class UrlWordsGenerator @Autowired()(dateFormatter: DateFormatter) {

  def makeUrlWordsFor(resource: Resource, publisher: Option[Website] = None): Option[String] = {
    resource match {
      case n: Newsitem => makeUrlWordsForNewsitem(n, publisher)
      case r: Resource => Some(makeUrlWordsFromName(r.title))
    }
  }

  def makeUrlWordsForTag(tag: Tag): String = {
    makeUrlWordsFromName(tag.getDisplayName)
  }

  private def makeUrlWordsForNewsitem(newsitem: Newsitem, publisher: Option[Website]): Option[String] = {
    val uri = new StringBuilder

    publisher.map { p =>
      uri.append("/" + makeUrlWordsFromName(p.title))
    }

    newsitem.date.map { d =>
      uri.append("/" + dateFormatter.yearMonthDayUrlStub(d))
      uri.append("/" + makeUrlWordsFromName(newsitem.title))
      uri.toString()
    }
  }

  def makeUrlWordsFromName(name: String): String = {
      val urlWords = new String(name)
      urlWords.replaceAll("\\(.*?\\)", "").trim.replaceAll(" ", "-").replaceAll("\\s", "").replaceAll("[^\\w-]", "").replaceAll("-+", "-").toLowerCase
  }

}
