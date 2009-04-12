package nz.co.searchwellington.views;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.NewsitemImpl;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.RssFeedable;

import org.springframework.web.servlet.View;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;

public class JSONView  implements View{

	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	public void render(Map model, HttpServletRequest req, HttpServletResponse res) throws Exception {
        List <RssFeedable> mainContent =  (List <RssFeedable>) model.get("main_content");        
		res.setContentType("text/plain");
		res.setCharacterEncoding("UTF-8");
	   
		XStream xstream = new XStream(new JettisonMappedXmlDriver() {
		    public HierarchicalStreamWriter createWriter(Writer writer) {
		        return new JsonWriter(writer, JsonWriter.DROP_ROOT_MODE);
		    }
		});
		
		xstream.setMode(XStream.NO_REFERENCES);
		xstream.alias("newitem", NewsitemImpl.class);

		List<Resource> items = new ArrayList<Resource>();		  
		for (RssFeedable item : mainContent) {			
			Newsitem jsonNewsitem = new NewsitemImpl();
			jsonNewsitem.setName(item.getRssItem().getTitle());
			jsonNewsitem.setUrl(item.getRssItem().getLink());
			jsonNewsitem.setDate(item.getRssItem().getPublishedDate());
			jsonNewsitem.setDescription(item.getRssItem().getDescription().getValue());
			items.add(jsonNewsitem);		  
		}
		  
		res.getOutputStream().print(xstream.toXML(items));        
		res.getOutputStream().flush();		
	}

}
