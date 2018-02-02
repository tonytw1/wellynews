package nz.co.searchwellington.repositories.elasticsearch

import nz.co.searchwellington.model.{Resource, Website}
import nz.co.searchwellington.repositories.HibernateResourceDAO
import nz.co.searchwellington.urls.UrlParser
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class PublisherGuessingService @Autowired()(resourceDAO: HibernateResourceDAO, var urlParser: UrlParser) {

  private val log = Logger.getLogger(classOf[PublisherGuessingService])

  def guessPublisherBasedOnUrl(url: String): Option[Website] = {

    def guessPossiblePublishersForUrl(url: String): Seq[Resource] = {
      val urlStem = urlParser.extractHostnameFrom(url)
      resourceDAO.getAllPublishersMatchingStem(urlStem, true)
    }

    val possiblePublishers = guessPossiblePublishersForUrl(url)

    (if (possiblePublishers.size == 1) {
      possiblePublishers.headOption

    } else if (possiblePublishers.size > 1) {

      val filtered = possiblePublishers.filter { p =>
        val pageUrl = p.url
        url.startsWith(pageUrl)
      }

      filtered.headOption

    } else {
      None

    }).map { p =>
      p.asInstanceOf[Website]
    }
  }

}
