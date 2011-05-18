package nz.co.searchwellington.controllers.admin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import nz.co.searchwellington.controllers.BaseMultiActionController;
import nz.co.searchwellington.repositories.keystore.KeyStore;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

public class AdminIndexController extends BaseMultiActionController {

    private static Logger log = Logger.getLogger(AdminIndexController.class);
	
	private KeyStore keystore;
	private CacheManager manager;

	
	public AdminIndexController(KeyStore keystore, CacheManager manager) {
		this.keystore = keystore;
		this.manager = manager;
	}


	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ModelAndView mv = new ModelAndView();
		mv.addObject("heading", "Admin index");
		
		mv.addObject("keystorecount", Long.toString(keystore.size()));

		for (String cacheName : manager.getCacheNames()) {
			Ehcache cache = manager.getEhcache(cacheName);
			log.info("Cache memsize '" + cacheName + "':" + Long.toString(cache.getMemoryStoreSize()));
			log.info("Cache hits '" + cacheName + "':" + Long.toString(cache.getHitCount()));
			log.info("Cache size '" + cacheName + "':" + Long.toString(cache.getSize()));
			log.info("Cache disksize '" + cacheName + "':" + Long.toString(cache.getDiskStoreSize()));			
		}
		
		mv.setViewName("adminindex");
		return mv;
	}

}
