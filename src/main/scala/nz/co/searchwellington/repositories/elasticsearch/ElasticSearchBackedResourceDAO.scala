package nz.co.searchwellington.repositories.elasticsearch

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.filters.RequestFilter
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.frontend.FrontendResource
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.geo.model.LatLong

@Component class ElasticSearchBackedResourceDAO @Autowired() (@Value("#{config['elasticsearch.host']}") elasticsearchHost: String,
                                                              @Value("#{config['elasticsearch.port']}") elasticsearchPort: Int,
                                                              val loggedInUserFilter: LoggedInUserFilter) {

  private val log = Logger.getLogger(classOf[RequestFilter])

  private val Index = "searchwellington"
  private val Resources = "resources"

  private val ID = "id"
  private val OWNER = "owner"
  private val HELD = "held"
  private val HTTP_STATUS = "httpStatus"
  private val PLACE = "place"
  private val LOCATION = "location"
  private val ALL = 1000
  private val PUBLISHER_NAME = "publisherName"
  private val INDEX_TAGS = "tags.id"
  private val DATE = "date"
  private val NAME = "name"
  private val TYPE = "type"
  private val LATEST_ITEM_DATE = "latestItemDate"

  val objectMapper = {
    val om = new ObjectMapper
    om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    om
  }

  def getGeotaggedCount(shouldShowBroken: Boolean): Long = {
    /*
    val builder = searchRequestBuilder(geotaggedNewsitems(shouldShowBroken)).setSize(0)
    val response = builder.execute.actionGet
    return response.getHits.getTotalHits
    */
    0 // TODO
  }

  def getPublisherFacetsNear(latLong: LatLong, radius: Double, shouldShowBroken: Boolean): Map[String, Int] = {
    /*
    val builder = searchRequestBuilder(geotaggedNearQuery(latLong, radius, shouldShowBroken)).addFacet(FacetBuilders.termsFacet(PUBLISHER_NAME).field(PUBLISHER_NAME).order(ComparatorType.COUNT).size(10)).execute.actionGet
    val facet = builder.getFacets.getFacets.get(PUBLISHER_NAME).asInstanceOf[TermsFacet]
    facet.getEntries.asInstanceOf[Seq[_ <: Entry]].map { entry =>
      ((entry.getTerm.string, entry.getCount))
    }.toMap
    */
    Map()  // TODO
  }

  def getNewsitemsNearDistanceFacet(latLong: LatLong, shouldShowBroken: Boolean): Map[Double, Long] = {
    /*
    val geoDistanceFacet = FacetBuilders.geoDistanceFacet("distance").field("location").lat(latLong.getLatitude).lon(latLong.getLongitude)
    geoDistanceFacet.unit(DistanceUnit.KILOMETERS)
    Range(1, 9).map { i =>
      geoDistanceFacet.addRange(0, i)
    }
    Range(10, 50).map { i =>
      geoDistanceFacet.addRange(0, i)
    }

    val builder = searchRequestBuilder(geotaggedNearQuery(latLong, 1000, shouldShowBroken)).setSize(0).addFacet(geoDistanceFacet)
    val response = builder.execute.actionGet
    val facets: GeoDistanceFacet = response.getFacets.facetsAsMap.get("distance").asInstanceOf[GeoDistanceFacet]

    import scala.collection.JavaConversions._
    facets.map { entry =>
      (entry.getTo, entry.getCount)
    }.toMap
    */
    Map()
  }

  def getGeotaggedNewsitemsNearCount(latLong: LatLong, radius: Double, shouldShowBroken: Boolean): Long = {
   // val builder = searchRequestBuilder(geotaggedNearQuery(latLong, radius, shouldShowBroken)).setSize(0)
   // val response = builder.execute.actionGet
    //return response.getHits.getTotalHits
    0 // TODO
  }

  def getTagFacetsForTag(tag: Tag): Map[String, Int] = {
    tagNewsitemsFacet(tag, INDEX_TAGS)
  }

  def getPublisherFacetsForTag(tag: Tag): Map[String, Int] = {
    tagNewsitemsFacet(tag, PUBLISHER_NAME)
  }

  def getCommentedNewsitemsForTagCount(tag: Tag, shouldShowBroken: Boolean): Int = {
    return 0
  }

  def getRecentCommentedNewsitemsForTag(tag: Tag, shouldShowBroken: Boolean, maxItems: Int): Seq[FrontendResource] = {
    Seq.empty //TODO
  }

  def getGeotaggedTags(shouldShowBroken: Boolean): Seq[Tag] = {
    Seq.empty //TODO
  }

  def getTwitteredNewsitems(startIndex: Int, maxItems: Int, shouldShowBroken: Boolean): Seq[FrontendResource] = {
   /* val builder = searchRequestBuilder(twitternMentionedNewsitemsQuery(shouldShowBroken)).setSize(maxItems)
    addDateDescendingOrder(builder)
    val response = builder.execute.actionGet
    deserializeFrontendResourceHits(response.getHits)
    */
    Seq()
  }

  def getTwitteredNewsitemsCount(shouldShowBroken: Boolean): Long = {
    /*
    val builder = searchRequestBuilder(twitternMentionedNewsitemsQuery(shouldShowBroken)).setSize(0)
    val response = builder.execute.actionGet
    response.getHits.getTotalHits
    */
    0 // TODO
  }

  def getRecentTwitteredNewsitemsForTag(maxItems: Int, shouldShowBroken: Boolean, tag: Tag): Seq[FrontendResource] = {
    Seq.empty //TODO
  }

  def getCommentedNewsitemsForTag(tag: Tag, shouldShowBroken: Boolean, maxNewsitems: Int, startIndex: Int): Seq[FrontendResource] = {
    Seq.empty // TODO
  }

  /*
  def getNewsitemsForMonth(month: Date, shouldShowBroken: Boolean): Seq[FrontendResource] = {
    val monthDateTime = new DateTime(month)
    val startOfMonth = monthDateTime.toDateMidnight.withDayOfMonth(1).toDateTime
    val endOfMonth = startOfMonth.plusMonths(1)
    val latestNewsitems = QueryBuilders.boolQuery.must(isNewsitem).must(QueryBuilders.rangeQuery(DATE).from(startOfMonth.toDate.getTime)).must(QueryBuilders.rangeQuery(DATE).to(endOfMonth.toDate.getTime))
    addShouldShowBrokenClause(latestNewsitems, shouldShowBroken)

    val builder = searchRequestBuilder(latestNewsitems).setSize(ALL)
    addDateDescendingOrder(builder)
    val response = builder.execute.actionGet
    deserializeFrontendResourceHits(response.getHits)
  }

  def getPublisherTagCombinerNewsitems(publisher: Website, tag: Tag, shouldShowBroken: Boolean, maxNewsitems: Int): Seq[FrontendResource] = {
    val publishertaggedNewsitems = QueryBuilders.boolQuery.must(QueryBuilders.termQuery(TYPE, "N"))
    publishertaggedNewsitems.must(hasTag(tag))
    publishertaggedNewsitems.must(hasPublisher(publisher))
    addShouldShowBrokenClause(publishertaggedNewsitems, shouldShowBroken)

    val builder = searchRequestBuilder(publishertaggedNewsitems).setSize(maxNewsitems)
    val response =  builder.execute.actionGet
    deserializeFrontendResourceHits(response.getHits)
  }

  def getHandTaggingsForUser(user: User, shouldShowBroken: Boolean): Seq[FrontendResource] = {
    Seq.empty // TODO
  }
  */

  /*
  def getNewspage(urlWords: String, shouldShowBroken: Boolean): Option[FrontendResource] = {
    val urlWordsQuery = QueryBuilders.boolQuery.must(QueryBuilders.termQuery("urlWords", urlWords))
    addShouldShowBrokenClause(urlWordsQuery, shouldShowBroken)
    val builder = searchRequestBuilder(urlWordsQuery).setSize(1)
    val response = builder.execute.actionGet
    deserializeFrontendResourceHits(response.getHits).headOption
  }
  */

  /*
  private def twitternMentionedNewsitemsQuery(shouldShowBroken: Boolean): FilteredQueryBuilder = {
    val latestNewsitems = QueryBuilders.boolQuery.must(isNewsitem)
    addShouldShowBrokenClause(latestNewsitems, shouldShowBroken)
    QueryBuilders.filtered(latestNewsitems, FilterBuilders.existsFilter("twitterMentions"))
  }
  */

  /*
  private def geotaggedNearQuery(latLong: LatLong, radius: Double, shouldShowBroken: Boolean): FilteredQueryBuilder = {
    val geotaggedNear = QueryBuilders.boolQuery.must(isNewsitem)
    addShouldShowBrokenClause(geotaggedNear, shouldShowBroken)
    val nearFilter = FilterBuilders.geoDistanceFilter(LOCATION).
      distance(radius.toString + "km").
      point(latLong.getLatitude, latLong.getLongitude)
    QueryBuilders.filtered(geotaggedNear, nearFilter)
  }
  */


  private def tagNewsitemsFacet(tag: Tag, facetField: String): Map[String, Int] = {
    /*
    val searchResponse = searchRequestBuilder(tagNewsitemsQuery(tag)).addFacet(FacetBuilders.termsFacet(facetField).field(facetField).order(ComparatorType.COUNT).size(10)).execute.actionGet
    val facet = searchResponse.getFacets.getFacets.get(facetField).asInstanceOf[TermsFacet]
    facet.getEntries.asInstanceOf[Seq[_ <: Entry]].map { entry =>
      (entry.getTerm.string, entry.getCount)
    }.toMap
    */
    Map()
  }

  /*
  private def tagNewsitemsQuery(tag: Tag): BoolQueryBuilder = {
    QueryBuilders.boolQuery.must(hasTag(tag)).must(isNewsitem)
  }
  */

  /*
  private def publisherNewsitemsRequest(publisher: Website, maxItems: Int, shouldShowBroken: Boolean, startIndex: Int): SearchRequestBuilder = {
    val publisherNewsitemsQuery = QueryBuilders.boolQuery
    publisherNewsitemsQuery.must(isNewsitem).must(hasPublisher(publisher))
    addShouldShowBrokenClause(publisherNewsitemsQuery, shouldShowBroken)

    val builder = searchRequestBuilder(publisherNewsitemsQuery).setFrom(startIndex).setSize(maxItems)
    addDateDescendingOrder(builder)
    builder
  }

  private def tagCombinerQuery(tags: Seq[Tag], shouldShowBroken: Boolean, maxNewsitems: Int): SearchRequestBuilder = {
    val taggedNewsitems = QueryBuilders.boolQuery.must(QueryBuilders.termQuery(TYPE, "N"))
    tags.map { tag =>
      taggedNewsitems.must(hasTag(tag))
    }
    addShouldShowBrokenClause(taggedNewsitems, shouldShowBroken)
    searchRequestBuilder(taggedNewsitems).setSize(maxNewsitems)
  }
  */

  /*
  private def geotaggedNewsitems(shouldShowBroken: Boolean): FilteredQueryBuilder = {
    val geotaggedNewsitems = QueryBuilders.boolQuery.must(isNewsitem)
    addShouldShowBrokenClause(geotaggedNewsitems, shouldShowBroken)
    QueryBuilders.filtered(geotaggedNewsitems, FilterBuilders.existsFilter(PLACE))
  }
  */

  /*
  private def hasPublisher(publisher: Website): TermQueryBuilder = {
    QueryBuilders.termQuery(PUBLISHER_NAME, publisher.title.getOrElse(""))
  }

  private def hasTag(tag: Tag): TermQueryBuilder = {
    QueryBuilders.termQuery(INDEX_TAGS, tag.getName)
  }

  private def isNewsitem: TermQueryBuilder = {
    QueryBuilders.termQuery(TYPE, "N")
  }

  private def isWatchlist: TermQueryBuilder = {
    QueryBuilders.termQuery(TYPE, "L")
  }

  private def isFeed: TermQueryBuilder = {
    QueryBuilders.termQuery(TYPE, "F")
  }
  */

  /*
  private def addShouldShowBrokenClause(query: BoolQueryBuilder, shouldShowBroken: Boolean) {
    /*
    if (!shouldShowBroken) {
      val contentIsOk: TermQueryBuilder = QueryBuilders.termQuery(HTTP_STATUS, "200")
      val contentIsApproved: TermQueryBuilder = QueryBuilders.termQuery(HELD, false)
      val contentIsPublic = QueryBuilders.boolQuery.must(contentIsOk).must(contentIsApproved)
      val userCanViewContent = QueryBuilders.boolQuery.minimumNumberShouldMatch(1).should(contentIsPublic)
      if (loggedInUserFilter.getLoggedInUser != null) {
        userCanViewContent.should(QueryBuilders.termQuery(OWNER, loggedInUserFilter.getLoggedInUser.getProfilename))
      }
      query.must(userCanViewContent)
    }
    query
    */
    query
  }
  */

  /*
  private def addDateDescendingOrder(searchRequestBuilder: SearchRequestBuilder) {
    searchRequestBuilder.addSort(DATE, SortOrder.DESC)
    searchRequestBuilder.addSort(ID, SortOrder.DESC)
  }

  private def addNameOrder(searchRequestBuilder: SearchRequestBuilder) {
    searchRequestBuilder.addSort(NAME, SortOrder.ASC)
  }

  private def addLatestFeedItemOrder(searchRequestBuilder: SearchRequestBuilder) {
    searchRequestBuilder.addSort(LATEST_ITEM_DATE, SortOrder.DESC)
  }

  private def searchRequestBuilder(query: QueryBuilder): SearchRequestBuilder = {
    //elasticSearchClientFactory.getClient.prepareSearch().setIndices(nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexUpdateService.INDEX).setTypes(nz.co.searchwellington.repositories.elasticsearch.ElasticSearchIndexUpdateService.TYPE).setQuery(query)
    null  // TODO
  }
  */


    
}
