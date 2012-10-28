package nz.co.searchwellington.controllers.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AdminIndexController {

	public AdminIndexController() {
	}
	
	@RequestMapping("/admin")
	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final ModelAndView mv = new ModelAndView("adminindex");
		mv.addObject("heading", "Admin index");	
		return mv;
	}

}
