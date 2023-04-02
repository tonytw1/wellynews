package nz.co.searchwellington.linkchecking

import nz.co.searchwellington.model.Newsitem
import nz.co.searchwellington.repositories.mongo.MongoRepository
import org.apache.commons.io.IOUtils
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.{mock, when}
import reactivemongo.api.commands.WriteResult

import java.nio.charset.StandardCharsets
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TwitterPhotoDetectorTest {

  private val pageWithTwitterImage = IOUtils.toString(
    this.getClass.getClassLoader.getResourceAsStream("rimutaka-incline-railway-news.html"),
    StandardCharsets.UTF_8.name
  )

  private val pageWithOpenGraphImage = IOUtils.toString(
    this.getClass.getClassLoader.getResourceAsStream("page-with-og-image.html"),
    StandardCharsets.UTF_8.name
  )

  private val pageWithOpenGraphImageProperty = IOUtils.toString(
    this.getClass.getClassLoader.getResourceAsStream("page-with-og-image-property.html"),
    StandardCharsets.UTF_8.name
  )

  private val mongoRepository = mock(classOf[MongoRepository])
  private val updateResult = mock(classOf[WriteResult])

  private val twitterPhotoDetector = new TwitterPhotoDetector(mongoRepository)

  @Test
  def canDetectTwitterImageFromMetaTags(): Unit = {
    val newsitem = Newsitem()
    when(mongoRepository.saveResource(newsitem)).thenReturn(Future.successful(updateResult))

    twitterPhotoDetector.process(newsitem, Some(pageWithTwitterImage), DateTime.now)
    assertEquals(Some("https://www.rimutaka-incline-railway.org.nz/sites/default/files/2020-12/20201017-a1328-IMG_6380.JPG"), newsitem.twitterImage)
  }

  @Test
  def canDetectOgImageFromMetaNameTags(): Unit = {
    val newsitem = Newsitem()
    when(mongoRepository.saveResource(newsitem)).thenReturn(Future.successful(updateResult))

    twitterPhotoDetector.process(newsitem, Some(pageWithOpenGraphImage), DateTime.now)

    assertEquals(Some("https://eyeofthefish.org/wp-content/uploads/2023/03/Gerard7.png"), newsitem.twitterImage)
  }

  @Test
  def canDetectOgImageFromMetaPropertyTags(): Unit = {
    val newsitem = Newsitem()
    when(mongoRepository.saveResource(newsitem)).thenReturn(Future.successful(updateResult))

    twitterPhotoDetector.process(newsitem, Some(pageWithOpenGraphImageProperty), DateTime.now)

    assertEquals(Some("https://www.wellingtonjudo.org.nz/wp-content/uploads/2018/05/tn_DSCF9240.jpg"), newsitem.twitterImage)
  }

}
