package nz.co.searchwellington.signin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import nz.co.searchwellington.model.User;

public interface SigninHandler {

	public ModelAndView getLoginView(HttpServletRequest request, HttpServletResponse response) throws Exception;
	
	public User getUserByExternalIdentifier(Object externalIdentifier);
	public Object getExternalUserIdentifierFromCallbackRequest(HttpServletRequest request);
	public void decorateUserWithExternalSigninIndenfier(User user, Object externalIdentifier);
	
}
