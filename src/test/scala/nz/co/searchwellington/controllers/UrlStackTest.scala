package nz.co.searchwellington.controllers

import nz.co.searchwellington.model.{SiteInformation, UrlWordsGenerator}
import nz.co.searchwellington.urls.UrlBuilder
import org.apache.struts.mock.MockHttpServletRequest
import org.junit.Assert.assertEquals
import org.junit.Test

class UrlStackTest {

  private val urlBuilder = new UrlBuilder(new SiteInformation(url = "https://wellynews.local"), new UrlWordsGenerator)
  private val urlStack = new UrlStack(urlBuilder)

  @Test
  def redirectionsFromEmptyStackShouldBeToHomepage: Unit = {
    val request = new MockHttpServletRequest()

    val redirectUrl = urlStack.getExitUrlFromStack(request)

    assertEquals("/", redirectUrl)
  }

  @Test
  def redirectionsFromTheStakShouldBeFullyQualifiedUrls: Unit = {
    val request = new MockHttpServletRequest()
    request.getSession.setAttribute("url", "/transport");

    val redirectUrl = urlStack.getExitUrlFromStack(request)

    assertEquals("https://wellynews.local/transport", redirectUrl)
  }

}
