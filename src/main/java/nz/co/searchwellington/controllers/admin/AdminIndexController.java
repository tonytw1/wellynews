package nz.co.searchwellington.controllers.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.BaseMultiActionController;
import nz.co.searchwellington.repositories.keystore.KeyStore;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AdminIndexController extends BaseMultiActionController {
	
	private KeyStore keystore;
	
	public AdminIndexController(KeyStore keystore) {
		this.keystore = keystore;
	}
	
	@RequestMapping("/admin/index")
	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final ModelAndView mv = new ModelAndView("adminindex");
		mv.addObject("heading", "Admin index");	
		mv.addObject("keystorecount", Long.toString(keystore.size()));		
		return mv;
	}

}
