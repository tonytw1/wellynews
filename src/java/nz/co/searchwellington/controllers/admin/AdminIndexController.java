package nz.co.searchwellington.controllers.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.BaseMultiActionController;
import nz.co.searchwellington.repositories.keystore.KeyStore;

import org.springframework.web.servlet.ModelAndView;

public class AdminIndexController extends BaseMultiActionController {

	private KeyStore keystore;

	

	public AdminIndexController(KeyStore keystore) {
		this.keystore = keystore;
	}


	public ModelAndView index(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		ModelAndView mv = new ModelAndView();
		mv.addObject("heading", "Admin index");
		mv.addObject("keystorecount", Long.toString(keystore.size()));
		mv.setViewName("adminindex");
		return mv;
	}

}
