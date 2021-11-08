package nz.co.searchwellington.submission

import nz.co.searchwellington.controllers.submission.EndUserInputs
import nz.co.searchwellington.urls.UrlCleaner
import org.junit.Assert._
import org.junit.Test
import org.mockito.Mockito.mock

class EndUserInputsTest extends EndUserInputs {

  val urlCleaner: UrlCleaner =  mock(classOf[UrlCleaner])

  @Test
  def shouldFlattenLoudCapsHeadlinesInUserSubmissions {
    assertEquals("The quick brown fox jumped over the lazy dog", processTitle("THE QUICK BROWN FOX JUMPED OVER THE LAZY DOG"))
  }

}