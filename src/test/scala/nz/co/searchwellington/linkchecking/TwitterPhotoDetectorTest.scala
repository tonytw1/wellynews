package nz.co.searchwellington.linkchecking

import nz.co.searchwellington.model.Newsitem
import org.apache.commons.io.IOUtils
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

import java.nio.charset.StandardCharsets
import scala.concurrent.ExecutionContext.Implicits.global

class TwitterPhotoDetectorTest {

  /*
  private val twitterPhotoUrl = "https://www.rimutaka-incline-railway.org.nz/sites/default/files/2020-12/20201017-a1328-IMG_6380.JPG"
  private val pageWithTwitterPhoto = IOUtils.toString(
    this.getClass.getClassLoader.getResourceAsStream("rimutaka-incline-railway-news.html"),
    StandardCharsets.UTF_8.name
  )

  private val twitterPhotoDetector = new TwitterPhotoDetector()

  @Test
  def canDetectTwitterPhotoUrlFromTwitterMetadata(): Unit = {
    val newsitem = Newsitem()

    twitterPhotoDetector.process(newsitem, Some(pageWithTwitterPhoto), DateTime.now)

    assertEquals(Some(twitterPhotoUrl), newsitem.twitterImage)
  }
  */

}
