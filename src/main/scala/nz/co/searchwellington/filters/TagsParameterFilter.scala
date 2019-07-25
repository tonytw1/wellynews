package nz.co.searchwellington.filters

import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@Component class TagsParameterFilter @Autowired() (var tagDAO: TagDAO, mongoRepository: MongoRepository)
  extends RequestAttributeFilter with ReasonableWaits {

  override def filter(request: HttpServletRequest): Unit = {
    if (request.getParameter("tags") != null) {
      val tagUrlWords = request.getParameterValues("tags")

      val tags = tagUrlWords.flatMap { tagUrlWords =>
        Await.result(mongoRepository.getTagByUrlWords(tagUrlWords), TenSeconds)
      }.toSeq

      request.setAttribute("tags", tags)
    }
  }

}