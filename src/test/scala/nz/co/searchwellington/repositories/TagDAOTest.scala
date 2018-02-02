package nz.co.searchwellington.repositories

import nz.co.searchwellington.model.Tag
import org.junit.Test
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{Cursor, DefaultDB, MongoConnection, MongoDriver}

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, MILLISECONDS}
import org.junit.Assert.{assertEquals, assertTrue}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, Macros}

class TagDAOTest {

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
      db.collection("tag")
    }

    val eventualCount: Future[Int] = tagCollection.flatMap { tagCollection =>
      tagCollection.count()
    }

    val count = Await.result(eventualCount, Duration(10000, MILLISECONDS))
    assertEquals(306, count)

    implicit def tagReader: BSONDocumentReader[Tag] = Macros.reader[Tag]

    val eventualCursor: Future[Cursor[Tag]] = tagCollection.map { tagCollection =>
      tagCollection.find(BSONDocument.empty).cursor[Tag]
    }

    val eventualDocuments = eventualCursor.flatMap { c =>
      c.collect[List](count)
    }

    val documents = Await.result(eventualDocuments, Duration(10000, MILLISECONDS))
    documents.map { d =>
      println(d)
    }
  }

}
