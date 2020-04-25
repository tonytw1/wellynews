package nz.co.searchwellington.geocoding.osm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.co.eelpieconsulting.common.geo.model.OsmId;
import uk.co.eelpieconsulting.common.geo.model.OsmType;

public class OsmIdParserTest {

	@Test
	public void canParseIdSlashTypeStyleOsmIdString() {
		OsmIdParser parser = new OsmIdParser();

		final OsmId parsedOsmId = parser.parseOsmId("123456/WAY");
		
		assertEquals(123456L, parsedOsmId.getId());
		assertEquals(OsmType.WAY, parsedOsmId.getType());
	}
	
	@Test
	public void canAccommodateVaryingLengthsOfTheOsmTypes() {
		OsmIdParser parser = new OsmIdParser();

		final OsmId parsedOsmId = parser.parseOsmId("123456/RELATION");
		
		assertEquals(OsmType.RELATION, parsedOsmId.getType());
	}
	
	@Test
	public void canAccommodateVaryingLengthsOfTheOsmIds() {
		OsmIdParser parser = new OsmIdParser();

		final OsmId parsedOsmId = parser.parseOsmId("123/WAY");
		
		assertEquals(123L, parsedOsmId.getId());
	}
	
}
