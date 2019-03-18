package nz.co.searchwellington.controllers

import java.util.UUID

import nz.co.searchwellington.controllers.submission.UrlProcessor
import nz.co.searchwellington.feeds.PlaceToGeocodeMapper
import nz.co.searchwellington.geocoding.osm.CachingNominatimGeocodingService
import nz.co.searchwellington.geocoding.osm.OsmIdParser
import nz.co.searchwellington.model.Feed
import nz.co.searchwellington.model.Newsitem
import nz.co.searchwellington.model.UrlWordsGenerator
import nz.co.searchwellington.model.User
import nz.co.searchwellington.repositories.HandTaggingDAO
import nz.co.searchwellington.repositories.HibernateResourceDAO
import nz.co.searchwellington.repositories.TagDAO
import nz.co.searchwellington.utils.UrlCleaner
import org.apache.struts.mock.MockHttpServletRequest
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.{verify, when}

class SubmissionProcessingServiceTest {

  private val FEED_NAME: String = "A feed"

  @Mock val urlCleaner: UrlCleaner = null
  @Mock val geocodeService: CachingNominatimGeocodingService = null
  @Mock val tagDAO: TagDAO = null
  @Mock val tagVoteDAO: HandTaggingDAO = null
  @Mock val resourceDAO: HibernateResourceDAO = null
  @Mock val resource: Newsitem = null
  val feed = Feed(id = UUID.randomUUID().toString, title = Some(FEED_NAME))
  @Mock val loggedInUser: User = null
  @Mock val urlProcessor: UrlProcessor = null
  @Mock val nominatimGeocodingService: CachingNominatimGeocodingService = null
  @Mock val osmIdParser: OsmIdParser = null
  @Mock val placeToGeocodeMapper: PlaceToGeocodeMapper = null

  private var urlWordsGenerator: UrlWordsGenerator = null
  private var request: MockHttpServletRequest = null
  private var submissionProcessingService: SubmissionProcessingService = null

  @Before def setup {
    MockitoAnnotations.initMocks(this)
    request = new MockHttpServletRequest
    urlWordsGenerator = new UrlWordsGenerator
    submissionProcessingService = new SubmissionProcessingService(nominatimGeocodingService, tagDAO, tagVoteDAO,
      resourceDAO, urlProcessor, osmIdParser, placeToGeocodeMapper)
  }

  @Test
  @throws[Exception]
  def acceptsEmbargoDates {
    val embargoDate = DateTime.now.toDate
    request.setAttribute("embargo_date", embargoDate)

    submissionProcessingService.processEmbargoDate(request, resource)

    verify(resource).setEmbargoedUntil(embargoDate)
  }

  @Test
  @throws[Exception]
  def shouldFlattenLoudCapsHeadlinesInUserSubmissions {
    request.addParameter("title", "THE QUICK BROWN FOX JUMPED OVER THE LAZY DOG")

    submissionProcessingService.processTitle(request, resource)

    verify(resource).setName("The quick brown fox jumped over the lazy dog")
  }

  @Test
  @throws[Exception]
  def shouldPopulateAcceptanceFieldsOnInitalSubmission {
    request.addParameter("acceptedFromFeed", urlWordsGenerator.makeUrlWordsFromName(FEED_NAME))
    when(resourceDAO.loadFeedByUrlWords("a-feed")).thenReturn(Some(feed))

    submissionProcessingService.processAcceptance(request, resource, loggedInUser)

    //verify(resource).setFeed(feed)
    //verify(resource).setAcceptedBy(loggedInUser)
  }

}
