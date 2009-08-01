package nz.co.searchwellington.openid;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.ParameterList;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Sample Consumer (Relying Party) implementation.
 */

public class SampleConsumer extends MultiActionController {

	public ConsumerManager manager;

    public SampleConsumer() throws ConsumerException {
        // instantiate a ConsumerManager object
    	manager = new ConsumerManager();
    }

	public ModelAndView login(HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String userSuppliedOpenID = "https://me.yahoo.com/tonymccrae";
		
		// discover the OpenID authentication server's endpoint URL
    	List discoveries = manager.discover(userSuppliedOpenID);
    	
    	// attempt to associate with the OpenID provider and retrieve one service endpoint for authentication
    	DiscoveryInformation discovered = manager.associate(discoveries);

		// store the discovery information in the user's session for later use
    	request.getSession().setAttribute("discovered", discovered);
    	    	
    	// define the return path
    	String returnURL = "http://localhost:8080/springapp/openid/return";
    	
    	// generate an AuthRequest message to be sent to the OpenID provider
    	AuthRequest authReq = manager.authenticate(discovered, returnURL);

    	// redirect the user to their provider for authentication    	
    	String destinationUrl = authReq.getDestinationUrl(true);
		return new ModelAndView(new RedirectView(destinationUrl));   	
    }

	
	
	public ModelAndView callback(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		ModelAndView mv = new ModelAndView("openid-return");
		
		// extract the parameters from the authentication response
		// (which comes in as a HTTP request from the OpenID provider)
		ParameterList openidResp = new ParameterList(request.getParameterMap());

		// retrieve the previously stored discovery information
		DiscoveryInformation discovered = (DiscoveryInformation) request.getSession().getAttribute("discovered");

		// extract the receiving URL from the HTTP request
		StringBuffer receivingURL = request.getRequestURL();
		String queryString = request.getQueryString();

		if (queryString != null && queryString.length() > 0) {
			receivingURL.append("?").append(request.getQueryString());
		}

		// verify the response
		VerificationResult verification = manager.verify(receivingURL.toString(), openidResp, discovered);

		// examine the verification result and extract the verified identifier
		Identifier verified = verification.getVerifiedId();
		
		if (verified != null) {
			System.out.println(verified);
			mv.addObject("id", verified);
		} else {
			mv.addObject("error", "Could not verify id");
			
		}	
		return mv;
	
	}

    
    
}