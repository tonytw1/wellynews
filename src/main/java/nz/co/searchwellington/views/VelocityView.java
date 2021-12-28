package nz.co.searchwellington.views;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.web.servlet.view.AbstractTemplateView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class VelocityView extends AbstractTemplateView {

    public VelocityView() {
    }

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
