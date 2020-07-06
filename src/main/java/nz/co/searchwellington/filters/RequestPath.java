package nz.co.searchwellington.filters;

import javax.servlet.http.HttpServletRequest;

public class RequestPath {

    public static String getPathFrom(HttpServletRequest request) {
        return request.getRequestURI();
    }

}
