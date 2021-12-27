package nz.co.searchwellington.views;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.spring.VelocityEngineUtils;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class VelocityView extends VelocityEngineUtils implements View {

    private final String viewname;
    private final Map<String, Object> attributes;
    private final VelocityEngine velocityEngine;

    public VelocityView(String viewname, VelocityEngine velocityEngine, Map<String, Object> attributes) {
        this.viewname = viewname;
        this.velocityEngine = velocityEngine;
        this.attributes = attributes;
    }

    @Override
    public void render(Map<String, ?> map, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        Map<String, Object> combined = new HashMap<>();
        combined.putAll(map);
        combined.putAll(attributes);
        mergeTemplate(velocityEngine, viewname, "UTF-8", combined, httpServletResponse.getWriter());
    }
}
