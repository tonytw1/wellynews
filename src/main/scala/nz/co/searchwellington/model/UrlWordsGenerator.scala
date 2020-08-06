package nz.co.searchwellington.model

import nz.co.searchwellington.forms.NewTag
import org.joda.time.DateTimeZone
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.dates.DateFormatter

@Component class UrlWordsGenerator {

  def makeUrlWordsForTag(newTag: NewTag): String = {  // TODO form backing object
    makeUrlWordsFromName(newTag.getDisplayName)
  }

  def makeUrlWordsFor(resource: Resource): Option[String] = {
    resource match {
      case n: Newsitem => makeUrlWordsForNewsitem(n)
      case r: Resource => r.title.map(makeUrlWordsFromName)
    }
  }

  private def makeUrlWordsForNewsitem(newsitem: Newsitem): Option[String] = {
    val uri = new StringBuilder

    newsitem.publisher.map { p =>
      uri.append("/" + makeUrlWordsFromName(p.toString))
    }

    newsitem.date.map { d =>
      val dateFormatter = new DateFormatter(DateTimeZone.UTC)
      uri.append("/" + dateFormatter.yearMonthDayUrlStub(d))
      uri.append("/" + makeUrlWordsFromName(newsitem.title.getOrElse("")))
      uri.toString()
    }
  }

  def makeUrlWordsFromName(name: String): String = {
      val urlWords = new String(name)
      urlWords.replaceAll("\\(.*?\\)", "").trim.replaceAll(" ", "-").replaceAll("\\s", "").replaceAll("[^\\w-]", "").replaceAll("-+", "-").toLowerCase
  }

}
