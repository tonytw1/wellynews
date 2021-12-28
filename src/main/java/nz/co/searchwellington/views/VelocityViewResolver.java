package nz.co.searchwellington.views;

import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.controllers.admin.AdminUrlBuilder;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.permissions.EditPermissionService;
import nz.co.searchwellington.urls.UrlBuilder;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractTemplateViewResolver;
import uk.co.eelpieconsulting.common.dates.DateFormatter;

@Component("viewResolver")
public class VelocityViewResolver extends AbstractTemplateViewResolver {

    private final VelocityEngine velocityEngine;
    private VelocityEngineUtils velocityEngineUtils;
    private AdminUrlBuilder adminUrlBuilder;
    private ColumnSplitter columnSplitter;
    private DateFormatter dateFormatter;
    private EditPermissionService editPermissionService;
    private RssUrlBuilder rssUrlBuilder;
    private SiteInformation siteInformation;
    private UrlBuilder urlBuilder;

    @Autowired
    public VelocityViewResolver(VelocityEngine velocityEngine,
                                VelocityEngineUtils velocityEngineUtils,
                                AdminUrlBuilder adminUrlBuilder,
                                ColumnSplitter columnSplitter,
                                DateFormatter dateFormatter,
                                EditPermissionService editPermissionService,
                                RssUrlBuilder rssUrlBuilder,
                                SiteInformation siteInformation,
                                UrlBuilder urlBuilder) {
        this.velocityEngine = velocityEngine;
        this.velocityEngineUtils = velocityEngineUtils;
        this.adminUrlBuilder = adminUrlBuilder;
        this.columnSplitter = columnSplitter;
        this.dateFormatter = dateFormatter;
        this.editPermissionService = editPermissionService;
        this.rssUrlBuilder = rssUrlBuilder;
        this.siteInformation = siteInformation;
        this.urlBuilder = urlBuilder;

        setViewClass(requiredViewClass());
        setSuffix(".vm");
    }

    /*
    @Override
    public View resolveViewName(String viewname, Locale locale) throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("urlBuilder", urlBuilder);
        attributes.put("adminUrlBuilder", adminUrlBuilder);
        attributes.put("columnSplitter", columnSplitter);
        attributes.put("dateFormatter", dateFormatter); // TODO Velocity has it's own one?
        attributes.put("editPermissionService", editPermissionService);
        attributes.put("escape", new EscapeTools());    // TODO still used?
        attributes.put("rssUrlBuilder", rssUrlBuilder);
        attributes.put("siteInformation", siteInformation);

        return new VelocityView(viewname + ".vm", velocityEngine, attributes, velocityEngineUtils);
    }

     */

    @Override
    protected Class<?> requiredViewClass() {
        return VelocityView.class;
    }

}
