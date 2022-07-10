package nz.co.searchwellington.views

import nz.co.searchwellington.model.Tag
import nz.co.searchwellington.model.frontend.FrontendNewsitem
import org.apache.velocity.spring.VelocityEngineFactoryBean
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue}
import org.junit.jupiter.api.Test
import org.springframework.ui.ModelMap

import java.io.StringWriter
import java.util.{Properties, UUID}
import scala.jdk.CollectionConverters._

class ViewsTest {

  @Test
  def shouldRenderTagPageHtmlViewsFromTemplate(): Unit = {
    val model = new ModelMap()
    model.addAttribute("tag", new Tag(name = "transport", display_name = "Transport"))
    val newsitems = (1 to 20).map { i =>
        FrontendNewsitem(id = UUID.randomUUID().toString, name = s"Newsitem $i", description = "A test newsitem", publisherName = Some("A publisher"))
    }
    model.addAttribute("main_content", newsitems.asJava)

    val velocityEngine = getVelocityEngine
    assertEquals("UTF-8", velocityEngine.getProperty("resource.default_encoding"))
    assertEquals("true", velocityEngine.getProperty("resource.loader.class.cache"))

    (1 to 10).foreach { _ =>
      val start = DateTime.now()
      val writer = new StringWriter()
      org.apache.velocity.spring.VelocityEngineUtils.mergeTemplate(velocityEngine, "tag.vm", "UTF-8", model, writer)
      //println("Rendered tag page in " + new Duration(start, DateTime.now()).getMillis + " ms")
      assertTrue(writer.toString.contains("<h2>Transport</h2>"))
    }
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
}
