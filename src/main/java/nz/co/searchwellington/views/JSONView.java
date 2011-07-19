package nz.co.searchwellington.views;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.ResourceEditController;
import nz.co.searchwellington.model.frontend.FrontendResource;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.View;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;

public class JSONView implements View {
	
	private Logger log = Logger.getLogger(ResourceEditController.class);

	public String getContentType() {
		return "text/plain";
	}

	@SuppressWarnings("unchecked")
	public void render(Map model, HttpServletRequest req, HttpServletResponse res) throws Exception {
		res.setContentType("text/plain");
		res.setCharacterEncoding("UTF-8");

		try {
			StringBuilder output = new StringBuilder();
	
			String jsonString = createJSONString(model);
			String callbackName = (String) model.get("callback");		
			if (callbackName != null) {
				final String callback = (String) callbackName;
				output.append(callback + "(");
				output.append(jsonString);
				output.append(");");
			} else {
				output.append(jsonString);
			}
		
		
			res.getWriter().print(output.toString());        
			res.getOutputStream().flush();

		} catch (Exception e) {
			log.error("An exception occured while attempting to output JSON view", e);
		}		
	}
	
	@SuppressWarnings("unchecked")
	private String createJSONString(Map model) {
		try {
			List <FrontendResource> mainContent =  (List <FrontendResource>) model.get("main_content");
			List<JSONFeedItem> jsonItems = new ArrayList<JSONFeedItem>();
			for (FrontendResource rssFeedable : mainContent) {
				JSONFeedItem jsonFeeditem;			
				if (rssFeedable.getGeocode() != null && rssFeedable.getGeocode().isValid()) {
					jsonFeeditem = new JSONFeedItem(
							rssFeedable.getName(), 
							rssFeedable.getUrl(), 
							rssFeedable.getDate(), 
							rssFeedable.getDescription(), rssFeedable.getGeocode().getLatitude(), rssFeedable.getGeocode().getLongitude());				
				} else {				
					jsonFeeditem = new JSONFeedItem(
							rssFeedable.getName(), 
							rssFeedable.getUrl(), 
							rssFeedable.getDate(), 
							rssFeedable.getDescription(), null, null);				
				}
				
				jsonItems.add(jsonFeeditem);
			}
			
			XStream xstream = new XStream(new JettisonMappedXmlDriver() {
			    public HierarchicalStreamWriter createWriter(Writer writer) {
			        return new JsonWriter(writer, JsonWriter.DROP_ROOT_MODE);
			    }
			});
			
			SingleValueConverter resourceDateConverter = new ResourceDateConvertor();
			xstream.registerLocalConverter(JSONFeedItem.class, "date", resourceDateConverter);	
			xstream.alias("date", java.sql.Date.class);
			
			JSONBucket bucket = new JSONBucket();
			bucket.setDescription((String) model.get("description"));
			if (model.get("main_content_total") != null) {
				bucket.setTotalItems((Integer) model.get("main_content_total"));
			}
			if (model.get("start_index") != null) {
				bucket.setShowingFrom((Integer) model.get("start_index"));
			}
			if (model.get("end_index") != null) {
				bucket.setShowingTo((Integer) model.get("end_index"));
			}
			
			bucket.setNewsitems(jsonItems);
			return xstream.toXML(bucket);
			
		} catch (Exception e) {
			log.error("Exception while trying to create JSON string", e);
		}
		
		return null;		
	}
	
}
