package nz.co.searchwellington.repositories

import java.util.Date

import nz.co.searchwellington.model.Tag
import org.joda.time.DateTime
import org.junit.Assert.assertEquals
import org.junit.Test
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{Cursor, DefaultDB, MongoConnection, MongoDriver}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, Macros}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, MILLISECONDS}
import scala.concurrent.{Await, Future}

class ResourceDAOTest {

  @Test
  def canConnectToMongoAndReadTags {
    val mongoUri = "mongodb://localhost:27017/wellynews"

    // Connect to the database: Must be done only once per application
    val driver = MongoDriver()
    val parsedUri = MongoConnection.parseURI(mongoUri)
    val connection = parsedUri.map(driver.connection(_))

    val futureConnection: Future[MongoConnection] = Future.fromTry(connection)

    def database: Future[DefaultDB] = futureConnection.flatMap(_.database("wellynews"))

    def tagCollection: Future[BSONCollection] = database.map { db =>
      db.collection("resource")
    }

    val eventualCount: Future[Int] = tagCollection.flatMap { tagCollection =>
      tagCollection.count()
    }

    val count = Await.result(eventualCount, Duration(10000, MILLISECONDS))
    assertEquals(73049, count)


    implicit def tagReader: BSONDocumentReader[MongoResource] = Macros.reader[MongoResource]

    val eventualCursor: Future[Cursor[MongoResource]] = tagCollection.map { tagCollection =>
      tagCollection.find(BSONDocument.empty).cursor[MongoResource]
    }

    val eventualDocuments = eventualCursor.flatMap { c =>
      c.collect[List](1000)
    }

    val documents = Await.result(eventualDocuments, Duration(10000, MILLISECONDS))
    documents.map { d =>
      println(d)
    }
  }

  case class MongoResource(id: Int, title: Option[String], page: Option[String], date: String)

}
