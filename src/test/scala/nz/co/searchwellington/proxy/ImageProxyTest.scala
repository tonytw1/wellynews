package nz.co.searchwellington.proxy

import nz.co.searchwellington.controllers.proxy.FileSystemCache
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

import java.nio.file.Files

class ImageProxyTest {

  val cache = {
    Files.createTempDirectory("imageproxytest")
    val tempFolder = Files.createTempDirectory("imageproxytest")
    new FileSystemCache[(MediaType, Array[Byte])]( tempFolder.toAbsolutePath.toString)
  }

  @Test
  def canRoundTripThroughFileSystemCache(): Unit = {
    val key = "somekey"
    val value = (MediaType.parseMediaType("image/jpeg"), "some image data".getBytes)

    cache.put(key, value)

    val readBack = cache.get(key)
    assertTrue(readBack.nonEmpty)
    assertEquals(value._1, readBack.get._1)
    assertTrue(value._2.sameElements(readBack.get._2))
  }

}
