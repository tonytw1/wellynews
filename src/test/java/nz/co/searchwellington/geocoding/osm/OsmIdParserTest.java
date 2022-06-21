package nz.co.searchwellington.geocoding.osm;

import org.junit.jupiter.api.Test;

import uk.co.eelpieconsulting.common.geo.model.OsmId;
import uk.co.eelpieconsulting.common.geo.model.OsmType;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
