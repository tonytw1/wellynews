package nz.co.searchwellington.repositories.elasticsearch

import java.io.IOException
import java.util.Date

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.{DeserializationFeature, JsonMappingException, ObjectMapper}
import nz.co.searchwellington.controllers.LoggedInUserFilter
import nz.co.searchwellington.model._
import nz.co.searchwellington.model.frontend.{FrontendFeed, FrontendNewsitem, FrontendResource}
import org.elasticsearch.action.search.SearchRequestBuilder
import org.elasticsearch.common.unit.DistanceUnit
import org.elasticsearch.index.query._
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.facet.FacetBuilders
import org.elasticsearch.search.facet.datehistogram.DateHistogramFacet
import org.elasticsearch.search.facet.geodistance.GeoDistanceFacet
import org.elasticsearch.search.facet.terms.TermsFacet
import org.elasticsearch.search.facet.terms.TermsFacet.{ComparatorType, Entry}
import org.elasticsearch.search.sort.SortOrder
import org.joda.time.{DateTime, DateTimeZone}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.geo.model.LatLong

@Component class ElasticSearchBackedResourceDAO @Autowired() (val elasticSearchClientFactory: ElasticSearchClientFactory, val loggedInUserFilter: LoggedInUserFilter) {

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

  def getLatestNewsitems(maxItems: Int, shouldShowBroken: Boolean, from: Int): Seq[FrontendResource] = {
    val latestNewsitems: BoolQueryBuilder = QueryBuilders.boolQuery.must(isNewsitem)
    addShouldShowBrokenClause(latestNewsitems, shouldShowBroken)
    val builder: SearchRequestBuilder = searchRequestBuilder(latestNewsitems).setFrom(from).setSize(maxItems)
    addDateDescendingOrder(builder)
    val response = builder.execute.actionGet
    deserializeFrontendResourceHits(response.getHits)
  }

  def getTaggedNewsitems(tag: Tag, shouldShowBroken: Boolean, startIndex: Int, maxItems: Int): Seq[FrontendResource] = {
    val query = tagNewsitemsQuery(tag)
    addShouldShowBrokenClause(query, shouldShowBroken)

    val builder = searchRequestBuilder(query).setFrom(startIndex).setSize(maxItems)
    addDateDescendingOrder(builder)
    val response = builder.execute.actionGet
    deserializeFrontendResourceHits(response.getHits)
  }

  def getPublisherNewsitems(publisher: Website, maxItems: Int, shouldShowBroken: Boolean, startIndex: Int): Seq[FrontendResource] = {
    val response = publisherNewsitemsRequest(publisher, maxItems, shouldShowBroken, startIndex).execute.actionGet
    deserializeFrontendResourceHits(response.getHits)
  }

  def getPublisherNewsitemsCount(publisher: Website, shouldShowBroken: Boolean): Long = {
    val response = publisherNewsitemsRequest(publisher, 0, shouldShowBroken, 0).execute.actionGet
    return response.getHits.getTotalHits
  }

  def getTaggedNewitemsCount(tag: Tag, shouldShowBroken: Boolean): Long = {
    val builder = searchRequestBuilder(tagNewsitemsQuery(tag))
    val response = builder.execute.actionGet
    return response.getHits.getTotalHits
  }

  def getLatestWebsites(maxItems: Int, shouldShowBroken: Boolean): Seq[FrontendResource] = {
    val isWebsite: TermQueryBuilder = QueryBuilders.termQuery(TYPE, "W")
    val websites: BoolQueryBuilder = QueryBuilders.boolQuery.must(isWebsite)
    addShouldShowBrokenClause(websites, shouldShowBroken)
    val justinWebsites: SearchRequestBuilder = searchRequestBuilder(websites).setSize(maxItems)
    addDateDescendingOrder(justinWebsites)
    val response = justinWebsites.execute.actionGet
    deserializeFrontendResourceHits(response.getHits)
  }

  def getTaggedWebsites(tags: Set[Tag], shouldShowBroken: Boolean, maxItems: Int): Seq[FrontendResource] = {
    val isWebsite: TermQueryBuilder = QueryBuilders.termQuery(TYPE, "W")
    val taggedWebsites: BoolQueryBuilder = QueryBuilders.boolQuery.must(isWebsite)
    for (tag <- tags) {
      taggedWebsites.must(hasTag(tag))
    }
    addShouldShowBrokenClause(taggedWebsites, shouldShowBroken)
    val builder = searchRequestBuilder(taggedWebsites).setSize(maxItems)
    addNameOrder(builder)
    val response = builder.execute.actionGet
    deserializeFrontendResourceHits(response.getHits)
  }

  def getTagWatchlist(tag: Tag, shouldShowBroken: Boolean): Seq[FrontendResource] = {
    val taggedWatchlists: BoolQueryBuilder = QueryBuilders.boolQuery.must(isWatchlist).must(hasTag(tag))
    val builder = searchRequestBuilder(taggedWatchlists).setSize(ALL)
    addShouldShowBrokenClause(taggedWatchlists, shouldShowBroken)
    addNameOrder(builder)
    val response = builder.execute.actionGet
    deserializeFrontendResourceHits(response.getHits)
  }

  def getTaggedFeeds(tag: Tag, shouldShowBroken: Boolean): Seq[FrontendResource] = {
    val taggedFeeds = QueryBuilders.boolQuery.must(isFeed).must(hasTag(tag))
    val builder = searchRequestBuilder(taggedFeeds).setSize(ALL)
    addShouldShowBrokenClause(taggedFeeds, shouldShowBroken)
    addNameOrder(builder)
    val response = builder.execute.actionGet
    deserializeFrontendResourceHits(response.getHits)
  }

  def getPublisherWatchlist(publisher: Website, shouldShowBroken: Boolean): Seq[FrontendResource] = {
    val publisherWatchlist: BoolQueryBuilder = QueryBuilders.boolQuery.must(isWatchlist).must(hasPublisher(publisher))
    addShouldShowBrokenClause(publisherWatchlist, shouldShowBroken)
    val builder = searchRequestBuilder(publisherWatchlist).setSize(ALL)
    addNameOrder(builder)
    val response = builder.execute.actionGet
    deserializeFrontendResourceHits(response.getHits)
  }

  def getPublisherFeeds(publisher: Website, shouldShowBroken: Boolean): Seq[FrontendResource] = {
    val publisherFeeds: BoolQueryBuilder = QueryBuilders.boolQuery.must(isFeed).must(hasPublisher(publisher))
    val builder = searchRequestBuilder(publisherFeeds).setSize(ALL)
    addShouldShowBrokenClause(publisherFeeds, shouldShowBroken)
    addNameOrder(builder)
    val response = builder.execute.actionGet
    deserializeFrontendResourceHits(response.getHits)
  }

  def getAllWatchlists(shouldShowBroken: Boolean): Seq[FrontendResource] = {
    val response = searchRequestBuilder(isWatchlist).setSize(ALL).execute.actionGet
    deserializeFrontendResourceHits(response.getHits)
  }

  def getAllFeeds(shouldShowBroken: Boolean, latestFirst: Boolean): Seq[FrontendResource] = {
    val feeds = QueryBuilders.boolQuery.must(isFeed)
    addShouldShowBrokenClause(feeds, shouldShowBroken)
    val builder = searchRequestBuilder(feeds).setSize(ALL)
    if (latestFirst) {
      addLatestFeedItemOrder(builder)
    }
    else {
      addNameOrder(builder)
    }
    val response = builder.execute.actionGet
    deserializeFrontendResourceHits(response.getHits)
  }

  def getGeotagged(startIndex: Int, maxItems: Int, shouldShowBroken: Boolean): Seq[FrontendResource] = {
    val builder = searchRequestBuilder(geotaggedNewsitems(shouldShowBroken)).setFrom(startIndex).setSize(maxItems)
    addDateDescendingOrder(builder)
    val response = builder.execute.actionGet
    deserializeFrontendResourceHits(response.getHits)
  }

  def getGeotaggedCount(shouldShowBroken: Boolean): Long = {
    val builder = searchRequestBuilder(geotaggedNewsitems(shouldShowBroken)).setSize(0)
    val response = builder.execute.actionGet
    return response.getHits.getTotalHits
  }

  def getGeotaggedNewsitemsNear(latLong: LatLong, radius: Double, shouldShowBroken: Boolean, startIndex: Int, maxItems: Int): Seq[FrontendResource] = {
    val builder = searchRequestBuilder(geotaggedNearQuery(latLong, radius, shouldShowBroken)).setFrom(startIndex).setSize(maxItems)
    addDateDescendingOrder(builder)
    val response = builder.execute.actionGet
    deserializeFrontendResourceHits(response.getHits)
  }

  def getPublisherFacetsNear(latLong: LatLong, radius: Double, shouldShowBroken: Boolean): Map[String, Int] = {
    val builder = searchRequestBuilder(geotaggedNearQuery(latLong, radius, shouldShowBroken)).addFacet(FacetBuilders.termsFacet(PUBLISHER_NAME).field(PUBLISHER_NAME).order(ComparatorType.COUNT).size(10)).execute.actionGet
    val facet = builder.getFacets.getFacets.get(PUBLISHER_NAME).asInstanceOf[TermsFacet]
    facet.getEntries.asInstanceOf[Seq[_ <: Entry]].map { entry =>
      ((entry.getTerm.string, entry.getCount))
    }.toMap
  }

  def getNewsitemsNearDistanceFacet(latLong: LatLong, shouldShowBroken: Boolean): Map[Double, Long] = {
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
  }

  def getGeotaggedNewsitemsNearCount(latLong: LatLong, radius: Double, shouldShowBroken: Boolean): Long = {
    val builder = searchRequestBuilder(geotaggedNearQuery(latLong, radius, shouldShowBroken)).setSize(0)
    val response = builder.execute.actionGet
    return response.getHits.getTotalHits
  }

  def getAllPublishers(shouldShowBroken: Boolean): Seq[PublisherContentCount] = {
    val searchResponse = searchRequestBuilder(QueryBuilders.boolQuery()).setSize(0).addFacet(FacetBuilders.termsFacet(PUBLISHER_NAME).field(PUBLISHER_NAME).order(ComparatorType.TERM).size(Integer.MAX_VALUE)).execute.actionGet
    val facet = searchResponse.getFacets.getFacets.get(PUBLISHER_NAME).asInstanceOf[TermsFacet]
    val entries = facet.getEntries
    import scala.collection.JavaConversions._
    entries.map { entry =>
      new PublisherContentCount(entry.getTerm.string, entry.getCount)
    }
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

  def getTaggedGeotaggedNewsitems(tag: Tag, maxItems: Int, shouldShowBroken: Boolean): Seq[FrontendResource] = {
    Seq.empty //TODO
  }

  def getGeotaggedTags(shouldShowBroken: Boolean): Seq[Tag] = {
    Seq.empty //TODO
  }

  def getCommentedNewsitems(maxItems: Int, shouldShowBroken: Boolean, b: Boolean, startIndex: Int): Seq[FrontendResource] = {
    Seq.empty //TODO
  }

  def getCommentedNewsitemsCount(shouldShowBroken: Boolean): Int = {
    return 0
  }

  def getCommentedTags(shouldShowBroken: Boolean): Seq[Tag] = {
    Seq.empty //TODO
  }

  def getTwitteredNewsitems(startIndex: Int, maxItems: Int, shouldShowBroken: Boolean): Seq[FrontendResource] = {
    val builder = searchRequestBuilder(twitternMentionedNewsitemsQuery(shouldShowBroken)).setSize(maxItems)
    addDateDescendingOrder(builder)
    val response = builder.execute.actionGet
    deserializeFrontendResourceHits(response.getHits)
  }

  def getTwitteredNewsitemsCount(shouldShowBroken: Boolean): Long = {
    val builder = searchRequestBuilder(twitternMentionedNewsitemsQuery(shouldShowBroken)).setSize(0)
    val response = builder.execute.actionGet
    response.getHits.getTotalHits
  }

  def getRecentTwitteredNewsitemsForTag(maxItems: Int, shouldShowBroken: Boolean, tag: Tag): Seq[FrontendResource] = {
    Seq.empty //TODO
  }

  def getArchiveMonths(shouldShowBroken: Boolean): Seq[ArchiveLink] = {
    val latestNewsitems = QueryBuilders.boolQuery.must(isNewsitem)
    addShouldShowBrokenClause(latestNewsitems, shouldShowBroken)
    val searchResponse = searchRequestBuilder(latestNewsitems).setSize(0).addFacet(FacetBuilders.dateHistogramFacet(DATE).field(DATE).interval("month")).execute.actionGet
    val dateFacet = searchResponse.getFacets.getFacets.get(DATE).asInstanceOf[DateHistogramFacet]

    import scala.collection.JavaConversions._
    dateFacet.getEntries.map { entry =>
      val monthDate = new DateTime(entry.getTime, DateTimeZone.UTC)
      new ArchiveLink(monthDate.toDate, entry.getCount)
    }.reverse
  }

  def getArchiveStatistics(shouldShowBroken: Boolean): Map[String, Int] = {
    val searchResponse = searchRequestBuilder(QueryBuilders.boolQuery()).addFacet(FacetBuilders.termsFacet(TYPE).field(TYPE)).execute.actionGet
    val facet = searchResponse.getFacets.getFacets.get(TYPE).asInstanceOf[TermsFacet]
    facet.getEntries.asInstanceOf[Seq[_ <: Entry]].map { entry =>
      (entry.getTerm.string, entry.getCount)
    }.toMap
  }

  def getCommentedNewsitemsForTag(tag: Tag, shouldShowBroken: Boolean, maxNewsitems: Int, startIndex: Int): Seq[FrontendResource] = {
    Seq.empty // TODO
  }

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

  def getTaggedNewsitems(tags: Seq[Tag], shouldShowBroken: Boolean, startIndex: Int, maxNewsitems: Int): Seq[FrontendResource] = {
    val builder = tagCombinerQuery(tags, shouldShowBroken, maxNewsitems)
    addNameOrder(builder)
    val response = builder.execute.actionGet
    deserializeFrontendResourceHits(response.getHits)
  }

  def getTaggedNewsitemsCount(tags: Seq[Tag], shouldShowBroken: Boolean): Long = {
    val searchRequestBuilder = tagCombinerQuery(tags, shouldShowBroken, 0)
    val response = searchRequestBuilder.execute.actionGet
    response.getHits.getTotalHits
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

  def getNewspage(urlWords: String, shouldShowBroken: Boolean): Option[FrontendResource] = {
    val urlWordsQuery = QueryBuilders.boolQuery.must(QueryBuilders.termQuery("urlWords", urlWords))
    addShouldShowBrokenClause(urlWordsQuery, shouldShowBroken)
    val builder = searchRequestBuilder(urlWordsQuery).setSize(1)
    val response = builder.execute.actionGet
    deserializeFrontendResourceHits(response.getHits).headOption
  }

  private def twitternMentionedNewsitemsQuery(shouldShowBroken: Boolean): FilteredQueryBuilder = {
    val latestNewsitems = QueryBuilders.boolQuery.must(isNewsitem)
    addShouldShowBrokenClause(latestNewsitems, shouldShowBroken)
    QueryBuilders.filtered(latestNewsitems, FilterBuilders.existsFilter("twitterMentions"))
  }

  private def geotaggedNearQuery(latLong: LatLong, radius: Double, shouldShowBroken: Boolean): FilteredQueryBuilder = {
    val geotaggedNear = QueryBuilders.boolQuery.must(isNewsitem)
    addShouldShowBrokenClause(geotaggedNear, shouldShowBroken)
    val nearFilter = FilterBuilders.geoDistanceFilter(LOCATION).
      distance(radius.toString + "km").
      point(latLong.getLatitude, latLong.getLongitude)
    QueryBuilders.filtered(geotaggedNear, nearFilter)
  }

  private def tagNewsitemsFacet(tag: Tag, facetField: String): Map[String, Int] = {
    val searchResponse = searchRequestBuilder(tagNewsitemsQuery(tag)).addFacet(FacetBuilders.termsFacet(facetField).field(facetField).order(ComparatorType.COUNT).size(10)).execute.actionGet
    val facet = searchResponse.getFacets.getFacets.get(facetField).asInstanceOf[TermsFacet]
    facet.getEntries.asInstanceOf[Seq[_ <: Entry]].map { entry =>
      (entry.getTerm.string, entry.getCount)
    }.toMap
  }

  private def tagNewsitemsQuery(tag: Tag): BoolQueryBuilder = {
    QueryBuilders.boolQuery.must(hasTag(tag)).must(isNewsitem)
  }

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

  private def geotaggedNewsitems(shouldShowBroken: Boolean): FilteredQueryBuilder = {
    val geotaggedNewsitems = QueryBuilders.boolQuery.must(isNewsitem)
    addShouldShowBrokenClause(geotaggedNewsitems, shouldShowBroken)
    QueryBuilders.filtered(geotaggedNewsitems, FilterBuilders.existsFilter(PLACE))
  }

  private def hasPublisher(publisher: Website): TermQueryBuilder = {
    QueryBuilders.termQuery(PUBLISHER_NAME, publisher.getName)
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

  private def addShouldShowBrokenClause(query: BoolQueryBuilder, shouldShowBroken: Boolean) {
    if (!shouldShowBroken) {
      val contentIsOk: TermQueryBuilder = QueryBuilders.termQuery(HTTP_STATUS, "200")
      val contentIsApproved: TermQueryBuilder = QueryBuilders.termQuery(HELD, false)
      val contentIsPublic: BoolQueryBuilder = QueryBuilders.boolQuery.must(contentIsOk).must(contentIsApproved)
      val userCanViewContent: BoolQueryBuilder = QueryBuilders.boolQuery.minimumNumberShouldMatch(1).should(contentIsPublic)
      if (loggedInUserFilter.getLoggedInUser != null) {
        userCanViewContent.should(QueryBuilders.termQuery(OWNER, loggedInUserFilter.getLoggedInUser.getProfilename))
      }
      query.must(userCanViewContent)
    }
    query
  }

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
    elasticSearchClientFactory.getClient.prepareSearch().setIndices(ElasticSearchIndexUpdateService.INDEX).setTypes(ElasticSearchIndexUpdateService.TYPE).setQuery(query)
  }

  private def deserializeFrontendResourceHits(hits: SearchHits): Seq[FrontendResource] = {
    hits.hits.map { next =>
      try {
        next.getSource.get(TYPE).toString match {
          case "N" => objectMapper.readValue(next.getSourceAsString, classOf[FrontendNewsitem])
          case "F" => objectMapper.readValue(next.getSourceAsString, classOf[FrontendFeed])
          case _ => objectMapper.readValue(next.getSourceAsString, classOf[FrontendResource])
        }
      }
      catch {
        case e: JsonParseException => {
          throw new RuntimeException(e)
        }
        case e: JsonMappingException => {
          throw new RuntimeException(e)
        }
        case e: IOException => {
          throw new RuntimeException(e)
        }
      }
    }
  }
    
}