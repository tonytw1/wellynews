package nz.co.searchwellington.model.frontend;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FrontendNewsitemTest {

	private static final String URL = "test";

	@Test
	public void canRoundTripThroughJackson() throws Exception {
		final ObjectMapper mapper = new ObjectMapper();		
		mapper.configure(MapperFeature.USE_ANNOTATIONS, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		final FrontendNewsitem newsitem = new FrontendNewsitem();
		newsitem.setFrontendImage(new FrontendImage(URL));
		final String json = mapper.writeValueAsString(newsitem);		
		final FrontendNewsitem result = mapper.readValue(json, FrontendNewsitem.class);

		assertNotNull(result.getFrontendImage());
	}
	
}
