package nz.co.searchwellington.linkchecking

import nz.co.searchwellington.model.Newsitem
import org.apache.commons.io.IOUtils
import org.joda.time.DateTime
import org.junit.Assert.assertEquals
import org.junit.Test

import scala.concurrent.ExecutionContext

class TwitterPhotoDetectorTest {

  private val twitterPhotoUrl = "https://www.rimutaka-incline-railway.org.nz/sites/default/files/2020-12/20201017-a1328-IMG_6380.JPG"
  private val pageWithTwitterPhoto = IOUtils.toString(this.getClass.getClassLoader.getResourceAsStream("rimutaka-incline-railway-news.html"))

  private val twitterPhotoDetector = new TwitterPhotoDetector()

  @Test
  def canDetectTwitterPhotoUrlFromTwitterMetadata(): Unit = {
    implicit val ec = ExecutionContext.Implicits.global

    val newsitem = Newsitem()
    twitterPhotoDetector.process(newsitem, Some(pageWithTwitterPhoto), DateTime.now)

    // TODO assertEquals(Some(twitterPhotoUrl), newsitem.image)
  }

}
