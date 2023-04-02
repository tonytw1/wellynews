package nz.co.searchwellington.linkchecking.cards

import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

import java.nio.charset.StandardCharsets

class SocialImageDetectorTest {

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

  private val twitterPhotoDetector = new SocialImageDetector

  @Test
  def canDetectTwitterImageFromMetaTags(): Unit = {
    val detected = twitterPhotoDetector.extractSocialImageUrlsFrom(pageWithTwitterImage)
    assertEquals(Some("https://www.rimutaka-incline-railway.org.nz/sites/default/files/2020-12/20201017-a1328-IMG_6380.JPG"), detected.flatMap(_.headOption))
  }

  @Test
  def canDetectOgImageFromMetaNameTags(): Unit = {
    val detected = twitterPhotoDetector.extractSocialImageUrlsFrom(pageWithOpenGraphImage)
    assertEquals(Some("https://eyeofthefish.org/wp-content/uploads/2023/03/Gerard7.png"), detected.flatMap(_.headOption))
  }

  @Test
  def canDetectOgImageFromMetaPropertyTags(): Unit = {
    val detected = twitterPhotoDetector.extractSocialImageUrlsFrom(pageWithOpenGraphImageProperty)
    assertEquals(Some("https://www.wellingtonjudo.org.nz/wp-content/uploads/2018/05/tn_DSCF9240.jpg"), detected.flatMap(_.headOption))
  }

}
