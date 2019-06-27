package nz.co.searchwellington.htmlparsing

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class CompositeLinkExtractor @Autowired()(linkExtractors: Array[LinkExtractor]) extends LinkExtractor {

  override def extractLinks (inputHTML: String): Seq[String] = {
    linkExtractors.toSeq.flatMap(_.extractLinks(inputHTML))
  }

}