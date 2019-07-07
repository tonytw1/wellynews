package nz.co.searchwellington.model

trait Tagged {

  def resource_tags: Seq[Tagging]
  def withTags(taggings: Seq[Tagging]): Resource

}
