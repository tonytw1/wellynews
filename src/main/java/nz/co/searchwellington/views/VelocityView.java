package nz.co.searchwellington.views;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.web.servlet.view.AbstractTemplateView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class VelocityView extends AbstractTemplateView {

    /*
    private final String viewname;
    private final Map<String, Object> attributes;
    private final VelocityEngine velocityEngine;
    private final VelocityEngineUtils velocityEngineUtils;
    */

    public VelocityView() {
    }

    /*
    public VelocityView(String viewname, VelocityEngine velocityEngine, Map<String, Object> attributes, VelocityEngineUtils velocityEngineUtils) {
        this.viewname = viewname;
        this.velocityEngine = velocityEngine;
        this.attributes = attributes;   // TODO need to restore helper attributes
        this.velocityEngineUtils = velocityEngineUtils;
    }
    */

    @Override
    protected void renderMergedTemplateModel(Map<String, Object> map, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        // TODO move to creation time
        VelocityEngine velocityEngine = BeanFactoryUtils.beanOfTypeIncludingAncestors(
                obtainApplicationContext(), VelocityEngine.class, true, false);
        VelocityEngineUtils velocityEngineUtils = BeanFactoryUtils.beanOfTypeIncludingAncestors(
                obtainApplicationContext(), VelocityEngineUtils.class, true, false);

        velocityEngineUtils.mergeTemplate(velocityEngine, this.getUrl(), "UTF-8", map, httpServletResponse.getWriter());
    }

}
