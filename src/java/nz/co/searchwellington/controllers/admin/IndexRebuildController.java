package nz.co.searchwellington.controllers.admin;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.BaseMultiActionController;
import nz.co.searchwellington.model.Twit;
import nz.co.searchwellington.repositories.ResourceRepository;
import nz.co.searchwellington.repositories.SolrIndexRebuildService;
import nz.co.searchwellington.twitter.TwitterService;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

public class IndexRebuildController extends BaseMultiActionController {

	Logger log = Logger.getLogger(IndexRebuildController.class);
    
	private SolrIndexRebuildService solrIndexRebuildService;
	private TwitterService twitterService;
	private ResourceRepository resourceDAO;
         
    public IndexRebuildController(SolrIndexRebuildService solrIndexRebuildService, TwitterService twitterService, ResourceRepository resourceDAO) {       
        this.solrIndexRebuildService = solrIndexRebuildService;
        this.twitterService = twitterService;
        this.resourceDAO = resourceDAO;
    }

    
    public ModelAndView build(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();                
        mv.setViewName("luceneIndexBuilder");    
        if (solrIndexRebuildService.buildIndex()) {
        	mv.addObject("message", "Created new index");
        } else {
        	mv.addObject("message", "Index rebuild failed");
        }
        return mv;
    }
    
        
    // TODO this is just here for quick wiring.
    @Transactional
    public ModelAndView refreshTweets(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	 ModelAndView mv = new ModelAndView();                
         mv.setViewName("luceneIndexBuilder");
    	if (twitterService.isConfigured()) {
    		refreshTweets();
        	mv.addObject("message", "Tweets refreshed");
    	}
    	mv.addObject("message", "Twitter service not configured; cannot refresh tweets");
    	return mv;
    }
    
    
    
	private void refreshTweets() {
		List<Twit> allLocalTwits = resourceDAO.getAllTweets();
		for (Twit twit : allLocalTwits) {
			if (twit.getDate() == null) {
				Twit status = twitterService.getTwitById(twit.getTwitterid());
				if (status != null) {
					resourceDAO.saveTweet(twit);
					log.info("Tweet date for '" + twit.getText() + "' updated to: " + twit.getDate());
				
				} else {
					log.warn("Could not load tweet #" + twit.getTwitterid().toString() + " from api");
				}
			}
		}
	}

}
