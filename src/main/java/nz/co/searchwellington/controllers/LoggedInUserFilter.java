package nz.co.searchwellington.controllers;

import nz.co.searchwellington.model.User;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component("loggedInUserFilter")
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LoggedInUserFilter {

    private static Logger log = Logger.getLogger(LoggedInUserFilter.class);

    private User loggedInUser;

    @Autowired
    public LoggedInUserFilter() {
        this.loggedInUser = null;
    }

    public void loadLoggedInUser(HttpServletRequest request) {
        log.debug("Looking for logged in user in session");
        if (request.getSession().getAttribute("user") != null) {
            User sessionUser = (User) request.getSession().getAttribute("user");
            log.info("Found user on session: " + sessionUser.getName());
            loggedInUser = sessionUser;
        } else {
            loggedInUser = null;
        }
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(HttpServletRequest request, User user) {
        log.info("Setting signed in user: " + user);
        request.getSession().setAttribute("user", user);
    }

}
