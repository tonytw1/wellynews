package nz.co.searchwellington.filters

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

// TODO depricate be using a url tagname instead of a form parameter - move to adminFilter?
// Used by the rssfeeds index page?
@Component
@Scope("request") class TagParameterFilter @Autowired()(mongoRepository: MongoRepository)
  extends RequestAttributeFilter with ReasonableWaits {

  override def filter(request: HttpServletRequest): Unit = {
    if (request.getParameter("tag") != null) {
      val urlWords = request.getParameter("tag")
      Await.result(mongoRepository.getTagByUrlWords(urlWords), TenSeconds).map { tag =>
        request.setAttribute("tag", tag)
      }
    }
  }
}