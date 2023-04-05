package nz.co.searchwellington.linkchecking.cards

import org.apache.commons.io.IOUtils
import org.junit.Assert.assertFalse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

import java.nio.charset.StandardCharsets

class SocialImageDetectorTest {

  private val socialImageDetector = new SocialImageDetector

  @Test
  def canDetectTwitterImageFromMetaTags(): Unit = {
    val detected = socialImageDetector.extractSocialImageUrlsFrom(loadAsString("rimutaka-incline-railway-news.html"))
    assertEquals(Some("https://www.rimutaka-incline-railway.org.nz/sites/default/files/2020-12/20201017-a1328-IMG_6380.JPG"), detected.flatMap(_.headOption))
  }

  @Test
  def canDetectOgImageFromMetaNameTags(): Unit = {
    val detected = socialImageDetector.extractSocialImageUrlsFrom(loadAsString("page-with-og-image.html"))
    assertEquals(Some("https://eyeofthefish.org/wp-content/uploads/2023/03/Gerard7.png"), detected.flatMap(_.headOption))
  }

  @Test
  def canDetectOgImageFromMetaPropertyTags(): Unit = {
    val detected = socialImageDetector.extractSocialImageUrlsFrom(loadAsString("page-with-og-image-property.html"))
    assertEquals(Some("https://www.wellingtonjudo.org.nz/wp-content/uploads/2018/05/tn_DSCF9240.jpg"), detected.flatMap(_.headOption))
  }

  private def loadAsString(filename: String) = {
    IOUtils.toString(
      this.getClass.getClassLoader.getResourceAsStream(filename),
      StandardCharsets.UTF_8.name
    )
  }

  @Test
  def shouldIgnoreNoneAbsoluteOgImageUrls(): Unit = {
    val detected = socialImageDetector.extractSocialImageUrlsFrom(loadAsString("page-with-relative-og-image.html"))
    assertFalse(detected.get.contains("/assets/Uploads/ShareImage/Lionesses-Djane-left-and-Zahra-right.JPG"))
  }

}
