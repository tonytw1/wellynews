package nz.co.searchwellington.views

import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.model.frontend.FrontendNewsitem
import org.apache.velocity.spring.{VelocityEngineFactoryBean, VelocityEngineUtils}
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.{MockHttpServletRequest, MockHttpServletResponse}
import org.springframework.ui.ModelMap
import org.springframework.web.servlet.View
import uk.co.eelpieconsulting.common.views.EtagGenerator
import uk.co.eelpieconsulting.common.views.rss.RssView

import java.io.StringWriter
import java.util.UUID
import scala.jdk.CollectionConverters._

@SpringBootTest
class ViewsTest @Autowired()(velocityEngineFactoryBean: VelocityEngineFactoryBean) {


  @Test
  def shouldRenderTagPageHtmlViewsFromTemplate(): Unit = {
    val model = new ModelMap()
    model.addAttribute("tag", new Tag(name = "transport", display_name = "Transport"))
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
    model.addAttribute("tag", new Tag(name = "transport", display_name = "Transport"))
    model.addAttribute("data", validNewsitems.asJava)
    val rssView: View = new RssView(new EtagGenerator(), "", "", "")
    val response = new MockHttpServletResponse()

    rssView.render(model, new MockHttpServletRequest(), response)

    val responseBody = new String(response.getContentAsByteArray)
    assertTrue(responseBody.contains("<title>Newsitem 1</title>"))
  }

  @Test
  def canRenderRssViewsWithURIFragmentsInImageUrl(): Unit = {
    val urlWithFragments = "https://i0.wp.com/guardiansofthebays.org.nz/wp-content/uploads/2022/09/Sunrise-over-Rongotai-and-the-airport.jpg?fit=1200%2C600&#038;ssl=1&#038;w=640"

    val model = new ModelMap()
    model.addAttribute("tag", new Tag(name = "transport", display_name = "Transport"))
    val newsitemWithOddImageURL = FrontendNewsitem(id = UUID.randomUUID().toString, name = s"Newsitem with odd image URI", description = "A test newsitem", publisherName = Some("A publisher"),
      url = urlWithFragments, twitterImage = urlWithFragments)

    model.addAttribute("data", Seq(newsitemWithOddImageURL).asJava)
    val rssView: View = new RssView(new EtagGenerator(), "", "", "")
    val response = new MockHttpServletResponse()

    rssView.render(model, new MockHttpServletRequest(), response)

    val responseBody = new String(response.getContentAsByteArray)
    assertTrue(responseBody.contains("<title>Newsitem with odd image URI</title>"))
    assertTrue(responseBody.contains("media:thumbnail url=\"https://i0.wp.com/guardiansofthebays.org.nz/wp-content/uploads/2022/09/Sunrise-over-Rongotai-and-the-airport.jpg?fit=1200%2C600\""))
  }

  private def getVelocityEngine = velocityEngineFactoryBean.createVelocityEngine()

  private def validNewsitems: Seq[FrontendNewsitem] = {
    (1 to 20).map { i =>
      FrontendNewsitem(id = UUID.randomUUID().toString, name = s"Newsitem $i", description = "A test newsitem", publisherName = Some("A publisher"))
    }
  }

}
