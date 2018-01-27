package nz.co.searchwellington.repositories

import java.util.List

import nz.co.searchwellington.model.Twit
import org.hibernate.SessionFactory
import org.hibernate.criterion.Restrictions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class TweetDAO @Autowired() (sessionFactory: SessionFactory) {

  def loadTweetByTwitterId(twitterId: Long): Twit = {
    sessionFactory.getCurrentSession.createCriteria(classOf[Twit]).add(Restrictions.eq("twitterid", twitterId)).setMaxResults(1).setCacheable(true).uniqueResult.asInstanceOf[Twit]
  }

  @SuppressWarnings(Array("unchecked")) def getAllTweets: Seq[Twit] = {
    import scala.collection.JavaConversions._
    sessionFactory.getCurrentSession.createCriteria(classOf[Twit]).list.asInstanceOf[List[Twit]]
  }

  def saveTwit(twit: Twit) {
    sessionFactory.getCurrentSession.saveOrUpdate(twit)
  }

}