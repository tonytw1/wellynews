package nz.co.searchwellington.controllers

import nz.co.searchwellington.ReasonableWaits
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.UUID
import scala.concurrent.{Await, ExecutionContext}

@Component class AnonUserService @Autowired() (mongoRepository: MongoRepository) extends ReasonableWaits {

  private val log = Logger.getLogger(classOf[AnonUserService])

  def createAnonUser()(implicit ec: ExecutionContext): User = {
    val anonUser = User(profilename = Some("anon" + UUID.randomUUID.toString), created = Some(DateTime.now.toDate))
    Await.result(mongoRepository.saveUser(anonUser), TenSeconds)
    log.info("Created new anon user: " + anonUser.profilename)
    anonUser
  }

}