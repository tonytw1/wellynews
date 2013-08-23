package nz.co.searchwellington.signin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.co.searchwellington.model.User;

import org.springframework.web.servlet.ModelAndView;

public interface SigninHandler {

	public ModelAndView getLoginView(HttpServletRequest request, HttpServletResponse response) throws Exception;
	
	public Object getExternalUserIdentifierFromCallbackRequest(HttpServletRequest request);
	public User getUserByExternalIdentifier(Object externalIdentifier);
	public void decorateUserWithExternalSigninIdentifier(User user, Object externalIdentifier);
	
}
