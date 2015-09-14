package nz.co.searchwellington.controllers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.annotations.Timed;
import nz.co.searchwellington.controllers.models.ContentModelBuilderService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.sun.syndication.io.FeedException;

@Controller
public class ContentController {

    private static final Logger log = Logger.getLogger(ContentController.class);

    private ContentModelBuilderService contentModelBuilder;
    private UrlStack urlStack;

    public ContentController() {
    }

    @Autowired
    public ContentController(ContentModelBuilderService contentModelBuilder, UrlStack urlStack) {
        this.contentModelBuilder = contentModelBuilder;
        this.urlStack = urlStack;
    }

    @RequestMapping(value = {"/", "/*", "/search", "/archive/*/*", "/*/comment", "/*/geotagged", "/feed/*", "/feeds/inbox", "/tags", "/tags/json", "/*/json", "/*/rss", "/*/*/*/*/*"})
    @Timed(timingNotes = "")
    public ModelAndView normal(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, FeedException, IOException {
        final ModelAndView mv = contentModelBuilder.populateContentModel(request);
        if (mv != null) {
            if (isHtmlView(mv)) {
                urlStack.setUrlStack(request);
            }
            return mv;
        }

        log.warn("Model was null; returning 404");
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return null;
    }

    private boolean isHtmlView(ModelAndView mv) {
        return mv.getViewName() != null;
    }

}
