package nz.co.searchwellington.repositories

import nz.co.searchwellington.model.Supression
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class SupressionDAO @Autowired() (mongoRepository: MongoRepository) {

  private val log = Logger.getLogger(classOf[SupressionDAO])

  def addSuppression(urlToSupress: String) {
    val suppression = new Supression(urlToSupress)

    if (suppression != null) {
      // sessionFactory.getCurrentSession.saveOrUpdate(suppression)
      log.info("Created suppression for: " + suppression.getUrl)
    }
  }

  def isSupressed(url: String): Boolean = {
    //val existingSupression = sessionFactory.getCurrentSession.createCriteria(classOf[Supression]).add(Restrictions.eq("url", url)).setMaxResults(1).uniqueResult.asInstanceOf[Supression]
    //existingSupression != null
    false // TODO
  }

  def removeSupressionForUrl(url: String) {
    //val existingSupression = sessionFactory.getCurrentSession.createCriteria(classOf[Supression]).add(Restrictions.eq("url", url)).setMaxResults(1).uniqueResult.asInstanceOf[Supression]
    //if (existingSupression != null) {
      //sessionFactory.getCurrentSession.delete(existingSupression)
    //}
  }

}
