package nz.co.searchwellington.controllers

import nz.co.searchwellington.model.{SiteInformation, UrlWordsGenerator}
import nz.co.searchwellington.urls.UrlBuilder
import org.joda.time.DateTimeZone
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import uk.co.eelpieconsulting.common.dates.DateFormatter

class UrlStackTest {

  private val urlBuilder = new UrlBuilder(new SiteInformation(url = "https://wellynews.local"), new UrlWordsGenerator(new DateFormatter(DateTimeZone.UTC)))
  private val urlStack = new UrlStack(urlBuilder)

  @Test
  def redirectionsFromEmptyStackShouldBeToHomepage(): Unit = {
    val request = new MockHttpServletRequest()

    val redirectUrl = urlStack.getExitUrlFromStack(request)

    assertEquals("https://wellynews.local/", redirectUrl)
  }

  @Test
  def redirectionsFromTheStakShouldBeFullyQualifiedUrls(): Unit = {
    val request = new MockHttpServletRequest()
    request.getSession.setAttribute("url", "/transport")

    val redirectUrl = urlStack.getExitUrlFromStack(request)

    assertEquals("https://wellynews.local/transport", redirectUrl)
  }

}
