package nz.co.searchwellington.model

trait Tagged {

  def resource_tags: Seq[Tagging]
  def withTaggings(taggings: Seq[Tagging]): Resource

}
