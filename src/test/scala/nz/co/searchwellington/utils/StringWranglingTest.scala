package nz.co.searchwellington.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StringWranglingTest extends StringWrangling {

  @Test
  def canFlattenLoudCaps(): Unit = {
    val loud = "THE QUICK BROWN FOX JUMPED OVER THE LAZY DOG"

    assertEquals("The quick brown fox jumped over the lazy dog", lowerCappedSentence(loud))
  }

}
