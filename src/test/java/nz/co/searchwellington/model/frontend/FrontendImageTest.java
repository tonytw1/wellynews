package nz.co.searchwellington.model.frontend;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FrontendImageTest {

	private static final String URL = "test";

	@Test
	public void canRoundTripThroughJackson() throws Exception {
		final ObjectMapper mapper = new ObjectMapper();		
		mapper.configure(MapperFeature.USE_ANNOTATIONS, true);
		
		final String json = mapper.writeValueAsString(new FrontendImage(URL));		
		final FrontendImage result = mapper.readValue(json, FrontendImage.class);

		assertEquals(URL, result.getUrl());
	}
	
}
