package nz.co.searchwellington.repositories

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Supression
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactivemongo.api.commands.WriteResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

@Component class SuppressionDAO @Autowired()(mongoRepository: MongoRepository) extends ReasonableWaits {

  private val log = LogFactory.getLog(classOf[SuppressionDAO])

  def addSuppression(urlToSupress: String): Unit = {
    log.info("Supression url: " + urlToSupress)
    Await.result(mongoRepository.saveSupression(Supression(url = urlToSupress)), TenSeconds)
  }

  def isSupressed(url: String): Future[Boolean] = {
    mongoRepository.getSupressionByUrl(url).map(_.nonEmpty)
  }

  def removeSupressionForUrl(url: String): WriteResult = {
    Await.result(mongoRepository.removeSupressionFor(url), TenSeconds)
  }

}
