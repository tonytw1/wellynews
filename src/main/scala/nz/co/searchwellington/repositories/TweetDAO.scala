package nz.co.searchwellington.repositories

import nz.co.searchwellington.model.Twit
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class TweetDAO @Autowired() (mongoRepository: MongoRepository) {

  def loadTweetByTwitterId(twitterId: Long): Twit = {
    // sessionFactory.getCurrentSession.createCriteria(classOf[Twit]).add(Restrictions.eq("twitterid", twitterId)).setMaxResults(1).setCacheable(true).uniqueResult.asInstanceOf[Twit]
    null // TODO
  }

  @SuppressWarnings(Array("unchecked")) def getAllTweets: Seq[Twit] = {
    // sessionFactory.getCurrentSession.createCriteria(classOf[Twit]).list.asInstanceOf[List[Twit]]
    Seq() //TODO
  }

  def saveTwit(twit: Twit) {
    // sessionFactory.getCurrentSession.saveOrUpdate(twit)
  }

}
