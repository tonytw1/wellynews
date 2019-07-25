package nz.co.searchwellington.filters

import com.google.common.base.Strings
import javax.servlet.http.HttpServletRequest
import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

@deprecated
@Component
@Scope("request") class PublisherParameterFilter @Autowired()(var mongoRepository: MongoRepository)
  extends RequestAttributeFilter with ReasonableWaits {

  override def filter(request: HttpServletRequest): Unit = {
    if (!(Strings.isNullOrEmpty(request.getParameter("publisher")))) {
      val publisherUrlWords = request.getParameter("publisher")
      Await.result(mongoRepository.getWebsiteByUrlwords(publisherUrlWords), TenSeconds).map { publisher =>
        request.setAttribute("publisher", publisher)
      }
    }
  }

}