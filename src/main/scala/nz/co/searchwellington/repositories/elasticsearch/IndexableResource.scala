package nz.co.searchwellington.repositories.elasticsearch

import nz.co.searchwellington.model.{Geocode, Resource}
import nz.co.searchwellington.tagging.IndexTagsService
import nz.co.searchwellington.urls.UrlParser

import scala.concurrent.{ExecutionContext, Future}

trait IndexableResource {

  def indexTagsService: IndexTagsService
  def urlParser: UrlParser

  def toIndexable(resource: Resource)(implicit ec: ExecutionContext): Future[(Resource, Seq[String], Seq[String],
    Option[Geocode], Option[String])] = {
    val eventualIndexTags = indexTagsService.getIndexTagsForResource(resource)
    val eventualGeocode = indexTagsService.getIndexGeocodeForResource(resource)
    for {
      indexTags <- eventualIndexTags
      geocode <- eventualGeocode
    } yield {
      val indexTagIds = indexTags.map(_._id.stringify)
      val handTagIds = resource.resource_tags.map(_.tag_id.stringify).distinct
      val hostname = urlParser.extractHostnameFrom(resource.page)
      (resource, indexTagIds, handTagIds, geocode, hostname)
    }
  }

}
