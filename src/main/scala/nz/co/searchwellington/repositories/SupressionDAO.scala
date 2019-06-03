package nz.co.searchwellington.repositories

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.Supression
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

@Component class SupressionDAO @Autowired() (mongoRepository: MongoRepository) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[SupressionDAO])

  def addSuppression(urlToSupress: String) {
    log.info("Supression url: " + urlToSupress)
    Await.result(mongoRepository.saveSupression(Supression(url = urlToSupress)), TenSeconds)
  }

  def isSupressed(url: String): Future[Boolean] = {
    mongoRepository.getSupressionByUrl(url).map(_.nonEmpty)
  }

  def removeSupressionForUrl(url: String) = {
    Await.result(mongoRepository.removeSupressionFor(url), TenSeconds)
  }

}
