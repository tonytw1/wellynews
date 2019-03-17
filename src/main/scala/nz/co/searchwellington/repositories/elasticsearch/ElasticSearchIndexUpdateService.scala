package nz.co.searchwellington.repositories.elasticsearch

import com.fasterxml.jackson.databind.{MapperFeature, ObjectMapper}
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component class ElasticSearchIndexUpdateService @Autowired()() {

  private val log = Logger.getLogger(classOf[ElasticSearchIndexUpdateService])

  val INDEX = "searchwellington" // TODO config
  val TYPE = "resources"

  val mapper = new ObjectMapper()
  mapper.configure(MapperFeature.USE_ANNOTATIONS, true)
  /*
    public void updateSingleContentItem(Resource contentItem) {
      log.debug("Updating content item: " + contentItem.getId());
      try {
        final Client client = elasticSearchClientFactory.getClient();
        prepateUpdateFor(contentItem, client).execute().actionGet();
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }

    public void deleteContentItem(Integer id) {
      log.info("Deleting content item: " + id);
      elasticSearchClientFactory.getClient().prepareDelete(INDEX, TYPE, Integer.toString(id)).setOperationThreaded(false).execute().actionGet();
    }

    public void updateMultipleContentItems(List<Resource> contentItems) {
      log.debug("Updating content items");
      if (contentItems.isEmpty()) {
        log.warn("Ignoring empty index update request");
        return;
      }

      final Client client = elasticSearchClientFactory.getClient();
      final BulkRequestBuilder bulkRequest = client.prepareBulk();
      for (Resource contentItem : contentItems) {
        try {
          bulkRequest.add(prepateUpdateFor(contentItem, client));
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }

      log.debug("Executing bulk request with " + bulkRequest.numberOfActions() + " actions");
      bulkRequest.execute().actionGet();
    }

    private IndexRequestBuilder prepateUpdateFor(Resource contentItem, Client client) throws JsonProcessingException {
      final FrontendResource frontendContentItem = frontendResourceMapper.createFrontendResourceFrom(contentItem);

      final String json = mapper.writeValueAsString(frontendContentItem);
      log.debug("Updating elastic search with json: " + json);
      return client.prepareIndex(INDEX, TYPE, Integer.toString(contentItem.getId())).setSource(json);
    }
    */
}
