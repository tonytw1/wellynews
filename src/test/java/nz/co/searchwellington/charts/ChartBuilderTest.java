package nz.co.searchwellington.charts;

import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

public class ChartBuilderTest extends TestCase {

	
	public void testShouldBuildPieChartUrl() throws Exception {		
		Map<String, Integer> data = new TreeMap<String, Integer>();
		data.put("apple", 3);
		data.put("carrot cake", 5);
		data.put("banana", 2);
		
		ChartBuilder chartbuilder = new ChartBuilder();		
		String chartUrl = chartbuilder.makePieChart(data);		
		assertTrue(chartUrl.contains("chd=t:3,2,5"));
		assertTrue(chartUrl.contains("chl=apple|banana|carrot+cake"));		
	}
}
