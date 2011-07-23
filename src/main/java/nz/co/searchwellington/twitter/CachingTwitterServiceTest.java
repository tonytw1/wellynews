package nz.co.searchwellington.twitter;

import static org.junit.Assert.assertEquals;
import net.sf.ehcache.CacheManager;
import nz.co.searchwellington.caching.MemcachedCache;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CachingTwitterServiceTest {
	
	private static final String USER_NAME = "SOMEUSER";

	private String PROFILE_IMAGE_URL = "http://images.twitter.com...";
	
	@Mock TwitterService twitterService;
	@Mock CacheManager manager;
	@Mock MemcachedCache memcachedCache;

	private CachingTwitterService service;
	
	@Before
	public void seup() {
		MockitoAnnotations.initMocks(this);
		service = new CachingTwitterService(twitterService, manager, memcachedCache);
	}

	@Test
	public void shouldTryProfileImageCacheBeforeFetchingFromLive() throws Exception {
		Mockito.when(memcachedCache.get("twitterprofileimage" + USER_NAME)).thenReturn(PROFILE_IMAGE_URL);

		final String twitterProfileImageUrl = service.getTwitterProfileImageUrlFor(USER_NAME);
		
		assertEquals(PROFILE_IMAGE_URL, twitterProfileImageUrl);		
	}

	@Test
	public void shouldRetrieveFromLiveIfProfileImageCacheMisses() throws Exception {
		Mockito.when(memcachedCache.get("twitterprofileimage" + USER_NAME)).thenReturn(null);
		Mockito.when(twitterService.getTwitterProfileImageUrlFor(USER_NAME)).thenReturn(PROFILE_IMAGE_URL);
		
		final String twitterProfileImageUrl = service.getTwitterProfileImageUrlFor(USER_NAME);
		
		assertEquals(PROFILE_IMAGE_URL, twitterProfileImageUrl);
	}
	
	@Test
	public void shouldCacheLiveResultsOnRetrieval() throws Exception {
		Mockito.when(memcachedCache.get("twitterprofileimage" + USER_NAME)).thenReturn(null);
		Mockito.when(twitterService.getTwitterProfileImageUrlFor(USER_NAME)).thenReturn(PROFILE_IMAGE_URL);
		
		service.getTwitterProfileImageUrlFor(USER_NAME);
		
		Mockito.verify(memcachedCache).put("twitterprofileimage" + USER_NAME, 3600 * 24, PROFILE_IMAGE_URL);
	}
}
