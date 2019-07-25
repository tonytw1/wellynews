package nz.co.searchwellington.controllers;

import nz.co.searchwellington.annotations.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import uk.co.eelpieconsulting.common.views.ViewFactory;

@Controller
public class PingController {

    private final ViewFactory viewFactory;

    @Autowired
    public PingController(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
    }

    @RequestMapping(value = "/healthz", method = RequestMethod.GET)
    @Timed(timingNotes = "")
    public ModelAndView ping() {
        final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
        mv.addObject("data", "ok");
        return mv;
    }

}