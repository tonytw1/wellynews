package nz.co.searchwellington.controllers.models

import nz.co.searchwellington.controllers.CommonModelObjectsService
import nz.co.searchwellington.controllers.models.helpers.{ArchiveModelBuilder, FeedModelBuilder, FeedsModelBuilder, GeotaggedModelBuilder, IndexModelBuilder, JustinModelBuilder, PublisherModelBuilder, PublisherTagCombinerModelBuilder, PublishersModelBuilder, SuggestionsModelBuilder, TagModelBuilder, TagsModelBuilder, WatchlistModelBuilder}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.views.ViewFactory

@Component class ContentModelBuilderServiceFactory @Autowired()(viewFactory: ViewFactory,
                                                                commonModelObjectsService: CommonModelObjectsService,
                                                                indexModelBuilder: IndexModelBuilder,
                                                                tagsModelBuilder: TagsModelBuilder,
                                                                tagModelBuilder: TagModelBuilder,
                                                                feedsModelBuilder: FeedsModelBuilder,
                                                                publishersModelBuilder: PublishersModelBuilder,
                                                                publisherModelBuilder: PublisherModelBuilder,
                                                                publisherTagCombinerModelBuilder: PublisherTagCombinerModelBuilder,
                                                                watchlistModelBuilder: WatchlistModelBuilder,
                                                                feedModelBuilder: FeedModelBuilder,
                                                                justinModelBuilder: JustinModelBuilder,
                                                                suggestionsModelBuilder: SuggestionsModelBuilder,
                                                                archiveModelBuilder: ArchiveModelBuilder,
                                                                searchModelBuilder: SearchModelBuilder,
                                                                geotaggedModelBuilder: GeotaggedModelBuilder) {

  def makeContentModelBuilderService(): ContentModelBuilderService = {
    val modelBuilders = Seq(indexModelBuilder, tagsModelBuilder, tagModelBuilder, feedsModelBuilder,
      publishersModelBuilder, publisherModelBuilder, publisherTagCombinerModelBuilder, watchlistModelBuilder, feedModelBuilder, justinModelBuilder, archiveModelBuilder,
      searchModelBuilder, suggestionsModelBuilder, geotaggedModelBuilder)

    new ContentModelBuilderService(viewFactory, commonModelObjectsService, modelBuilders)
  }

}
