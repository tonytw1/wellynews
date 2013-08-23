package nz.co.searchwellington.geocoding.osm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.co.eelpieconsulting.common.geo.model.OsmId;
import uk.co.eelpieconsulting.common.geo.model.OsmType;

public class OsmIdParserTest {

	@Test
	public void canParseIdSlashTypeStyleOsmIdString() throws Exception {
		OsmIdParser parser = new OsmIdParser();

		final OsmId parsedOsmId = parser.parseOsmId("123456/way");
		
		assertEquals(123456L, parsedOsmId.getId());
		assertEquals(OsmType.way, parsedOsmId.getType());
	}
	
	@Test
	public void canAccomodateVaryingLengthsOfTheOsmTypes() throws Exception {
		OsmIdParser parser = new OsmIdParser();

		final OsmId parsedOsmId = parser.parseOsmId("123456/relation");
		
		assertEquals(OsmType.relation, parsedOsmId.getType());
	}
	
	@Test
	public void canAccomodateVaryingLengthsOfTheOsmIds() throws Exception {
		OsmIdParser parser = new OsmIdParser();

		final OsmId parsedOsmId = parser.parseOsmId("123/way");
		
		assertEquals(123L, parsedOsmId.getId());
	}
	
}
