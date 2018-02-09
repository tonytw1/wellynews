package nz.co.searchwellington.repositories

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class SupressionService @Autowired() (suppressionDAO: SupressionDAO) {

  def suppressUrl(urlToSupress: String): Unit = if (!suppressionDAO.isSupressed(urlToSupress)) suppressionDAO.addSuppression(urlToSupress)

  def unsupressUrl(url: String): Unit = suppressionDAO.removeSupressionForUrl(url)
  
}
