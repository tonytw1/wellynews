package nz.co.searchwellington.openid;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.controllers.UrlBuilder;
import nz.co.searchwellington.controllers.UrlStack;
import nz.co.searchwellington.model.User;
import nz.co.searchwellington.model.UserImpl;
import nz.co.searchwellington.repositories.UserRepository;

import org.apache.log4j.Logger;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.ParameterList;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Sample Consumer (Relying Party) implementation.
 */

public class OpenIDLoginController extends MultiActionController {
	
	Logger log = Logger.getLogger(OpenIDLoginController.class);

	private static final String OPENID_CLAIMED_IDENTITY_PARAMETER = "openid_claimed_identity";
	public ConsumerManager manager;
	private UrlBuilder urlBuilder;
	private UrlStack urlStack;
	private UserRepository userDAO;

    public OpenIDLoginController(UrlBuilder urlBuilder, UrlStack urlStack, UserRepository userDAO) throws ConsumerException {
        // instantiate a ConsumerManager object
    	manager = new ConsumerManager();
    	this.urlBuilder = urlBuilder;
    	this.urlStack = urlStack;
    	this.userDAO = userDAO;
    
    }

	public ModelAndView login(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO make parameter based
		
		if (request.getParameter(OPENID_CLAIMED_IDENTITY_PARAMETER) != null) {		
			final String userSuppliedOpenID = request.getParameter(OPENID_CLAIMED_IDENTITY_PARAMETER);		
			try {
				// discover the OpenID authentication server's endpoint URL
				List discoveries = manager.discover(userSuppliedOpenID);
				
				// attempt to associate with the OpenID provider and retrieve one service endpoint for authentication
				DiscoveryInformation discovered = manager.associate(discoveries);
	
				// store the discovery information in the user's session for later use
				request.getSession().setAttribute("discovered", discovered);
	    	    	
				// define the return path
				String returnURL = urlBuilder.getOpenIDCallbackUrl();
	    	
				// generate an AuthRequest message to be sent to the OpenID provider
				AuthRequest authReq = manager.authenticate(discovered, returnURL);
	
				// redirect the user to their provider for authentication    	
				String destinationUrl = authReq.getDestinationUrl(true);
				return new ModelAndView(new RedirectView(destinationUrl));
				
			} catch (Exception e) {
				log.warn("Exception will processing claimed identifier: " + userSuppliedOpenID, e);
			}
		}
		
		return new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)));
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
			final String username = verified.getIdentifier();
			log.info("Verfied identifer: " + username);
			
			mv.addObject("id", verified);			
			User user = userDAO.getUser(username);
			
			if (user == null) {				
				// Create new user
				User newUser = new UserImpl(username);
				userDAO.saveUser(newUser);
				setUser(request, newUser);
				return new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)));
			}
			
			setUser(request, user);			
						
		} else {
			mv.addObject("error", "Could not verify id");			
		}
		
		return new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)));
	}

	private void setUser(HttpServletRequest request, User user) {
		request.getSession().setAttribute("user", user);
		log.info("Logged in user: " + user.getUsername());
		request.getSession().setAttribute("login_prompt", null);
	}
	
}