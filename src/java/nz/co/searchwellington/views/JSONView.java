package nz.co.searchwellington.views;

import java.io.Writer;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.PublishedResourceImpl;
import nz.co.searchwellington.model.ResourceImpl;
import nz.co.searchwellington.model.RssFeedable;

import org.springframework.web.servlet.View;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;

public class JSONView  implements View{

	public String getContentType() {
		return "text/plain";
	}

	@SuppressWarnings("unchecked")
	public void render(Map model, HttpServletRequest req, HttpServletResponse res) throws Exception {
        List <RssFeedable> mainContent =  (List <RssFeedable>) model.get("main_content");        
		res.setContentType("text/plain");
		res.setCharacterEncoding("UTF-8");
	   		
		XStream xstream = new XStream(new JettisonMappedXmlDriver() {
		    public HierarchicalStreamWriter createWriter(Writer writer) {
		        return new JsonWriter(writer, JsonWriter.DROP_ROOT_MODE);
		    }
		});
		
		SingleValueConverter resourceDateConverter = new ResourceDateConvertor();
		xstream.registerLocalConverter(ResourceImpl.class, "date", resourceDateConverter);
		
		xstream.omitField(ResourceImpl.class, "id");
		xstream.omitField(ResourceImpl.class, "httpStatus");
		xstream.omitField(ResourceImpl.class, "lastScanned");
		xstream.omitField(ResourceImpl.class, "lastChanged");
		xstream.omitField(ResourceImpl.class, "tags");		
		xstream.omitField(ResourceImpl.class, "technoratiCount");
		xstream.omitField(ResourceImpl.class, "discoveredFeeds");
		xstream.omitField(ResourceImpl.class, "liveTime");

		xstream.omitField(PublishedResourceImpl.class, "publisher");		
		xstream.omitField(NewsitemImpl.class, "commentFeed");
		
		xstream.alias("date", java.sql.Date.class);
		
		JSONBucket bucket = new JSONBucket();
		if (model.get("main_content_total") != null) {
			bucket.setTotalItems((Integer) model.get("main_content_total"));
		}
		if (model.get("start_index") != null) {
			bucket.setShowingFrom((Integer) model.get("start_index"));
		}
		if (model.get("end_index") != null) {
			bucket.setShowingTo((Integer) model.get("end_index"));
		}
		
		bucket.setNewsitems(mainContent);
		res.getOutputStream().print(xstream.toXML(bucket));        
		res.getOutputStream().flush();		
	}

}
