package nz.co.searchwellington.views;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;


public class AjaxXMLView implements View {

    public String getContentType() {
        return null;
    }
    
	public void render(Map model, HttpServletRequest req,
			HttpServletResponse res) throws Exception {
        
		res.setContentType("text/xml");      
        res.setCharacterEncoding("UTF-8");        
        res.getOutputStream().print((String) model.get("response"));        
		res.getOutputStream().flush();
	}

}
