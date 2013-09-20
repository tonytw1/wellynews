package nz.co.searchwellington.widgets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import nz.co.searchwellington.model.FeedAcceptancePolicy;

import org.junit.Test;

public class AcceptanceWidgetFactoryTest {

	private final AcceptanceWidgetFactory factory = new AcceptanceWidgetFactory();

	@Test
	public void acceptanceDropDownValuesShouldBeEnumValues() throws Exception {		
		final String dropdownHtml = factory.createAcceptanceSelect(null);

		assertTrue(dropdownHtml.contains("value='ACCEPT'"));
	}
	
	@Test
	public void acceptanceDropDownShouldMarkCurrentlySelected() throws Exception {				
		final String dropdownHtml = factory.createAcceptanceSelect(FeedAcceptancePolicy.SUGGEST);
		
		assertTrue(dropdownHtml.contains("option selected value='SUGGEST'"));
		assertFalse(dropdownHtml.contains("option selected value='ACCEPT'"));
	}
	
}
