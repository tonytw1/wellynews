package nz.co.searchwellington.model

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.dates.DateFormatter

@Component class UrlWordsGenerator @Autowired()(dateFormatter: DateFormatter) {

  def makeUrlWordsFor(resource: Resource, publisher: Option[Website] = None): String = {
    resource match {
      case n: Newsitem => makeUrlWordsForNewsitem(n, publisher)
      case r: Resource => makeUrlWordsFromName(r.title)
    }
  }

  def makeUrlWordsForTag(tag: Tag): String = {
    makeUrlWordsFromName(tag.getDisplayName)
  }

  private def makeUrlWordsForNewsitem(newsitem: Newsitem, publisher: Option[Website]): String = {
    val publisherComponent = publisher.map { p =>
      makeUrlWordsFromName(p.title)
    }
    val dateComponent = Some(dateFormatter.yearMonthDayUrlStub(newsitem.date))
    val titleComponent = Some(makeUrlWordsFromName(newsitem.title))

    Seq(publisherComponent, dateComponent, titleComponent).flatten.mkString("/")
  }

  def makeUrlWordsFromName(name: String): String = {
      val urlWords = new String(name)
      urlWords.replaceAll("\\(.*?\\)", "").trim.replaceAll(" ", "-").replaceAll("\\s", "").replaceAll("[^\\w-]", "").replaceAll("-+", "-").toLowerCase
  }

}
