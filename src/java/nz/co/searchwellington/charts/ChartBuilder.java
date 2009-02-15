package nz.co.searchwellington.charts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nz.co.searchwellington.utils.UrlFilters;

import org.apache.commons.lang.StringUtils;

public class ChartBuilder {

	private static String CHART_STEM_URL = "http://chart.apis.google.com/chart?";
	
	public String makePieChart(Map<String, Integer> data) {		
		List<String> keys = new ArrayList<String>();
		for (String key : data.keySet()) {
			keys.add(UrlFilters.encode(key));
		}
		
		List<Integer> values = new ArrayList<Integer>(data.values());
						
		StringBuffer chartUrl = new StringBuffer(CHART_STEM_URL);
		chartUrl.append("cht=p&");
		chartUrl.append("chs=250x130&");		
		
		chartUrl.append("chl=");
		chartUrl.append(StringUtils.join(keys.iterator(), '|'));
		chartUrl.append("&");
		
		chartUrl.append("chd=t:");
		chartUrl.append(StringUtils.join(values.iterator(), ','));		
		return chartUrl.toString();	
	}

    public String makeGoogleMeterChart(int percentage) {
        StringBuffer chartUrl = new StringBuffer(CHART_STEM_URL);
        chartUrl.append("cht=gom&");
        chartUrl.append("chs=250x130&");
        chartUrl.append("chd=t:" + percentage);       
        return chartUrl.toString();
    }
	
}
