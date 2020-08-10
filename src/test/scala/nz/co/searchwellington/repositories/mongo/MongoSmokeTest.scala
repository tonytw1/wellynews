package nz.co.searchwellington.repositories.mongo

import java.util.{Date, UUID}

import nz.co.searchwellington.ReasonableWaits
import org.joda.time.DateTime
import org.junit.Assert.assertEquals
import org.junit.Test
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.{BSONDateTime, BSONDocument, BSONHandler, BSONValue, Macros}
import reactivemongo.api.{MongoConnection, MongoDriver}
import reactivemongo.bson.DefaultBSONHandlers

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future._
import scala.util.Try

class MongoSmokeTest extends ReasonableWaits with DefaultBSONHandlers {

  val databaseName = "wellynews-" + UUID.randomUUID().toString
  val mongoRepository = new MongoRepository("mongodb://localhost:27017/" + databaseName)

  @Test
  def canRoundTripCaseClasses: Unit = {
    // Reactive Mongo upgrades are hard. This test provides a sandbox to investigate breaking changes
    val databaseName = "wellynews-" + UUID.randomUUID().toString
    val mongoUri = "mongodb://localhost:27017/" + databaseName

    val driver = MongoDriver()
    val parsedUri = MongoConnection.parseURI(mongoUri)

    val eventualDatabase = fromTry {
      parsedUri.map { uri =>
        uri.db.map { db =>
          driver.connection(uri).database(db)
        }.getOrElse {
          failed(new RuntimeException("No database given in Mongo URI"))
        }
      }
    }.flatten

    val db = Await.result(eventualDatabase, OneMinute)

    val mehCollection: BSONCollection = db.collection("meh")

    val meh = Meh("Test", DateTime.now.toDate)

    // TODO This feels wrong; why is the reactive mongo supplied one private?
    implicit object DateHandler extends BSONHandler[Date] {
      override def readTry(bson: BSONValue): Try[Date] = {
        bson match {
          case d: BSONDateTime =>
            scala.util.Success(new DateTime(d.value).toDate)
          case _ => scala.util.Failure(new RuntimeException)
        }
      }

      override def writeTry(d: Date): Try[BSONValue] = {
        scala.util.Success(BSONDateTime(d.getTime))
      }
    }

    implicit def mehHandler = Macros.handler[Meh]

    Await.result(mehCollection.insert.one(meh), TenSeconds)

    val one = mehCollection.find(BSONDocument.empty).one[Meh]
    val maybeDocument = Await.result(one, TenSeconds)
    assertEquals("Test", maybeDocument.get.title)
  }

  case class Meh(title: String, created: Date)

}
