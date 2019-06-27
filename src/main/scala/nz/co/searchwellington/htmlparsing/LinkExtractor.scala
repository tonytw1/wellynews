package nz.co.searchwellington.htmlparsing

trait LinkExtractor {
  def extractLinks(inputHTML: String): Seq[String]
}