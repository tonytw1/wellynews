package nz.co.searchwellington.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.model.User;
import nz.co.searchwellington.repositories.UserRepository;

import org.apache.log4j.Logger;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.view.RedirectView;


public class LoginController extends MultiActionController {
    
    Logger log = Logger.getLogger(LoginController.class);
  
    public ConsumerManager manager;
    
    private UserRepository userDAO;
    private UrlStack urlStack;


    protected LoginController(UserRepository userDAO, UrlStack urlStack) throws ConsumerException {
        this.userDAO = userDAO;
        this.urlStack = urlStack;
        
        manager = new ConsumerManager();
    }
   

    public ModelAndView prompt(HttpServletRequest request, HttpServletResponse response) {        
        ModelAndView modelAndView = new ModelAndView();
        request.getSession().setAttribute("login_prompt", "1");
        log.debug("login_prompt set on session");
        setRedirect(modelAndView, request);
                
        return modelAndView;
    }


    
    
    public ModelAndView login(HttpServletRequest request, HttpServletResponse response) {     
        ModelAndView modelAndView = new ModelAndView();
        
        final String username = request.getParameter("username");
        final String password = request.getParameter("password");
        
        if (username != null && password != null) {
            log.info("Unsetting login_prompt.");
            request.getSession().setAttribute("login_prompt", null);
            log.info("login_prompt is: " + request.getSession().getAttribute("login_prompt"));
            
            User testUser = userDAO.getUser(username, password);
            if (testUser != null) {
                request.getSession().setAttribute("user", testUser);
                log.info("Logged in user: " + testUser.getUsername());
            }
            
            
        } else {        
            request.getSession().setAttribute("user", null); 
            request.getSession().setAttribute("login_prompt", null);
        }
        
        setRedirect(modelAndView, request);                
        return modelAndView;
    }
    
    
    // TODO make safe all failure modes.
    public ModelAndView auth(HttpServletRequest request, HttpServletResponse response) {        
        ModelAndView mv = new ModelAndView();        
        
        String usersOpenIDUrl = request.getParameter("openid");
                                
        try {
            // configure the return_to URL where your application will receive
            // the authentication responses from the OpenID provider
            String returnToUrl = "http://www.wellington.gen.nz/openid/verify";
         
            // perform discovery on the user-supplied identifier
            log.info("Users url is: " + usersOpenIDUrl);
            List discoveries = manager.discover(usersOpenIDUrl);

            // attempt to associate with the OpenID provider
            // and retrieve one service endpoint for authentication
            DiscoveryInformation discovered = manager.associate(discoveries);

            // store the discovery information in the user's session
            request.getSession().setAttribute("openid-disc", discovered);

            // obtain a AuthRequest message to be sent to the OpenID provider
            AuthRequest authReq = manager.authenticate(discovered, returnToUrl);
          
            // Option 1: GET HTTP-redirect to the OpenID Provider endpoint
            // The only method supported in OpenID 1.x
            // redirect-URL usually limited ~2048 bytes
            final String redirectionUrl = authReq.getDestinationUrl(true);
            log.info("Redirecting to: " + redirectionUrl);
            mv.setView(new RedirectView(redirectionUrl));
            return mv;
         
        } catch (OpenIDException e) {
            // present error to the user            
        }
               
        return null;
        
    }
    
    
    
    public ModelAndView verify(HttpServletRequest request, HttpServletResponse response) {        
        ModelAndView mv = new ModelAndView();
        
        try {
            // extract the parameters from the authentication response
            // (which comes in as a HTTP request from the OpenID provider)
            ParameterList responseParameters = new ParameterList(request.getParameterMap());
    
            // retrieve the previously stored discovery information
            DiscoveryInformation discovered = (DiscoveryInformation) request.getSession().getAttribute("openid-disc");
    
            // extract the receiving URL from the HTTP request
            StringBuffer receivingURL = request.getRequestURL();
            String queryString = request.getQueryString();
            if (queryString != null && queryString.length() > 0)
                receivingURL.append("?").append(request.getQueryString());
    
                // verify the response; ConsumerManager needs to be the same
                // (static) instance used to place the authentication request
                VerificationResult verification = manager.verify(
                    receivingURL.toString(),
                    responseParameters, discovered);
    
                // examine the verification result and extract the verified identifier
                Identifier verified = verification.getVerifiedId();
                if (verified != null) {
                    AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse();
                    log.info("Verified correctly.");
                    log.info("Verified indentifer: " + verified.getIdentifier());
                    log.info("Auth success identity: " + authSuccess.getIdentity());
                    
                    //setRedirect(mv, request); 
                    return mv; // success
                }
        } catch (Exception e) {
            log.error(e);
        }
            
        log.info("Failed to verfied.");
        //setRedirect(mv, request); 
        return mv;
        }
    
    
    
    
    @SuppressWarnings("unchecked")
    public ModelAndView logout(HttpServletRequest request, HttpServletResponse response) {
        
        ModelAndView modelAndView = new ModelAndView();
        request.getSession().setAttribute("user", null); 
        request.getSession().setAttribute("login_prompt", null);                        
        setRedirect(modelAndView, request);                
        return modelAndView;
    }
    
    
    private void setRedirect(ModelAndView modelAndView, HttpServletRequest request) {
        modelAndView.setView(new RedirectView(urlStack.getExitUrlFromStack(request)));
    }
    
    
}
