package nz.co.searchwellington.views

import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.model.frontend.FrontendNewsitem
import org.apache.http.client.utils.URIBuilder
import org.apache.velocity.spring.{VelocityEngineFactoryBean, VelocityEngineUtils}
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test
import org.springframework.mock.web.{MockHttpServletRequest, MockHttpServletResponse}
import org.springframework.ui.ModelMap
import org.springframework.web.servlet.View
import uk.co.eelpieconsulting.common.views.EtagGenerator
import uk.co.eelpieconsulting.common.views.rss.RssView

import java.io.StringWriter
import java.net.URL
import java.util.{Properties, UUID}
import scala.jdk.CollectionConverters._

class ViewsTest {

  @Test
  def shouldRenderTagPageHtmlViewsFromTemplate(): Unit = {
    val model = new ModelMap()
    model.addAttribute("tag", Tag(name = "transport", display_name = "Transport"))
    model.addAttribute("main_content", validNewsitems.asJava)

    val velocityEngine = getVelocityEngine
    assertEquals("UTF-8", velocityEngine.getProperty("resource.default_encoding"))
    assertEquals("true", velocityEngine.getProperty("resource.loader.class.cache"))

    val writer = new StringWriter()

    VelocityEngineUtils.mergeTemplate(velocityEngine, "tag.vm", "UTF-8", model, writer)

    assertTrue(writer.toString.contains("<h2>Transport</h2>"))
  }

  @Test
  def canRenderRssViews(): Unit = {
    val model = new ModelMap()
    model.addAttribute("tag", Tag(name = "transport", display_name = "Transport"))
    model.addAttribute("data", validNewsitems.asJava)
    val rssView: View = new RssView(new EtagGenerator(), "", "", "")
    val response = new MockHttpServletResponse()

    rssView.render(model, new MockHttpServletRequest(), response)

    val responseBody = new String(response.getContentAsByteArray)
    assertTrue(responseBody.contains("<title>Newsitem 1</title>"))
  }

  // TODO can we get this from the actual bean?
  private def getVelocityEngine = {
    val velocityEngineFactory = new VelocityEngineFactoryBean
    val vp = new Properties
    vp.setProperty("resource.loader", "class")

    vp.setProperty("resource.loader.class.cache", "true")
    // When resource.manager.cache.default_size is set to 0, then the default implementation uses the standard Java ConcurrentHashMap.
    vp.setProperty("resource.manager.cache.default_size", "0")

    vp.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader")
    vp.setProperty("velocimacro.library", "spring.vm")
    velocityEngineFactory.setVelocityProperties(vp)
    velocityEngineFactory.createVelocityEngine()
  }

  private def validNewsitems: Seq[FrontendNewsitem] = {
    (1 to 20).map { i =>
      FrontendNewsitem(id = UUID.randomUUID().toString, name = s"Newsitem $i", description = "A test newsitem", publisherName = Some("A publisher"))
    }
  }

}
