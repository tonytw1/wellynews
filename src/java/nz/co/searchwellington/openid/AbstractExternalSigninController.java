package nz.co.searchwellington.openid;

import javax.servlet.http.HttpServletRequest;

import nz.co.searchwellington.model.User;

import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

public abstract class AbstractExternalSigninController extends MultiActionController {

	abstract protected Object getExternalUserIdentifierFromCallbackRequest(HttpServletRequest request);
	abstract protected void decorateUserWithExternalSigninIndenfier(User user, Object externalIdentifier);
	
}
