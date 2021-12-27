package nz.co.searchwellington.views;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.spring.VelocityEngineUtils;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.support.RequestContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class VelocityView extends VelocityEngineUtils implements View {

    // TODO is this still exists in a Spring abstract class we should work towards extending from it
    private static final String SPRING_MACRO_REQUEST_CONTEXT_ATTRIBUTE = "springMacroRequestContext";

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

        // Expose RequestContext instance for Spring macros.

        ServletContext servletContext = null;   // TODO Should be sourced from a Spring abstract class to be sure
        combined.put(SPRING_MACRO_REQUEST_CONTEXT_ATTRIBUTE,
                new RequestContext(httpServletRequest, httpServletResponse, servletContext, combined));

        httpServletResponse.setContentType("text/html;charset=UTF-8");
        mergeTemplate(velocityEngine, viewname, "UTF-8", combined, httpServletResponse.getWriter());
    }
}
