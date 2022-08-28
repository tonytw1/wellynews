package nz.co.searchwellington.repositories.elasticsearch

import nz.co.searchwellington.model.Resource
import nz.co.searchwellington.model.geo.Geocode
import nz.co.searchwellington.tagging.IndexTagsService
import nz.co.searchwellington.urls.UrlParser

import scala.concurrent.{ExecutionContext, Future}

trait IndexableResource {

  def indexTagsService: IndexTagsService
  def urlParser: UrlParser

  def toIndexable(resource: Resource)(implicit ec: ExecutionContext): Future[IndexResource] = {
    val eventualIndexTags = indexTagsService.getIndexTagsForResource(resource)
    val eventualGeocode = indexTagsService.getIndexGeocodeForResource(resource)
    for {
      indexTags <- eventualIndexTags
      geocode <- eventualGeocode
    } yield {
      val indexTagIds = indexTags.map(_._id.stringify)
      val handTagIds = resource.resource_tags.map(_.tag_id.stringify).distinct
      val hostname = urlParser.extractHostnameFrom(resource.page)
      IndexResource(resource, indexTagIds, handTagIds, geocode, hostname)
    }
  }
}

case class IndexResource(resource: Resource, indexTagIds: Seq[String], handTagIds: Seq[String],
                         geocode: Option[Geocode], hostname: Option[String])