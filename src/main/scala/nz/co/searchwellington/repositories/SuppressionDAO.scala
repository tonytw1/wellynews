package nz.co.searchwellington.repositories

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Supression
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Component class SuppressionDAO @Autowired()(mongoRepository: MongoRepository) extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[SuppressionDAO])

  def addSuppression(urlToSupress: String): Future[Boolean] = {
    log.info("Supression url: " + urlToSupress)
    mongoRepository.saveSupression(Supression(url = urlToSupress)).map(_.writeErrors.isEmpty)
  }

  def isSupressed(url: String): Future[Boolean] = {
    mongoRepository.getSupressionByUrl(url).map(_.nonEmpty)
  }

  def removeSupressionForUrl(url: String): Future[Boolean] = {
    mongoRepository.removeSupressionFor(url).map(_.writeErrors.isEmpty)
  }

}
