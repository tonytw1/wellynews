package nz.co.searchwellington.controllers.models

import nz.co.searchwellington.controllers.models.helpers._
import nz.co.searchwellington.repositories.ContentRetrievalService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.views.ViewFactory

@Component class ContentModelBuilderServiceFactory @Autowired()(viewFactory: ViewFactory,
                                                                contentRetrievalService: ContentRetrievalService,
                                                                indexModelBuilder: IndexModelBuilder,
                                                                tagsModelBuilder: TagsModelBuilder,
                                                                tagModelBuilder: TagModelBuilder,
                                                                tagCombinerModelBuilder: TagCombinerModelBuilder,
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
    val modelBuilders = Seq(indexModelBuilder, tagsModelBuilder, tagModelBuilder, tagCombinerModelBuilder,
      feedsModelBuilder, publishersModelBuilder, publisherModelBuilder, publisherTagCombinerModelBuilder, watchlistModelBuilder, feedModelBuilder, justinModelBuilder, archiveModelBuilder,
      searchModelBuilder, suggestionsModelBuilder, geotaggedModelBuilder)

    new ContentModelBuilderService(viewFactory, contentRetrievalService, modelBuilders)
  }

}
