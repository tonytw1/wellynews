package nz.co.searchwellington.tagging

import com.google.common.collect.Lists
import nz.co.searchwellington.model.taggingvotes.{TaggingVote, HandTagging}
import nz.co.searchwellington.model.{Newsitem, NewsitemImpl, Tag, TagBuilder}
import nz.co.searchwellington.repositories.{HandTaggingDAO}
import org.junit.Assert._
import org.junit.{Before, Test}
import org.mockito.Mockito.when
import org.mockito.{Mock, MockitoAnnotations}

class TaggingReturnsOfficerServiceTest {

  private var placesTag: Tag = null
  private var aroValleyTag: Tag = null
  private var aroValleyNewsitem: Newsitem = null

  @Mock private var handTaggingDAO: HandTaggingDAO  = null

  private var taggingReturnsOfficerService: TaggingReturnsOfficerService = null

  @Before def setUp {
    MockitoAnnotations.initMocks(this)
    placesTag = new TagBuilder().name("places").displayName("Places").build
    aroValleyTag = new TagBuilder().name("arovalley").displayName("Aro Valley").parent(placesTag).build
    placesTag.addChild(aroValleyTag)

    taggingReturnsOfficerService = new TaggingReturnsOfficerService(handTaggingDAO);
  }

  @Test def compliedTagsShouldContainAtLeastOneCopyOfEachManuallyAppliedTag {
    aroValleyNewsitem = new NewsitemImpl(1, "Test newsitem", null, ".. Student flats in the Aro Valley... Test", null, null, null)
    val handTags: java.util.List[HandTagging] = Lists.newArrayList(new HandTagging(-1, aroValleyNewsitem, null, aroValleyTag))
    when(handTaggingDAO.getHandTaggingsForResource(aroValleyNewsitem)).thenReturn(handTags)

    var tags: List[TaggingVote] = taggingReturnsOfficerService.complieTaggingVotes(aroValleyNewsitem)

    assertTrue(tags.contains(aroValleyTag));
  }

}