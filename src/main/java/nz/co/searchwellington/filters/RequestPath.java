package nz.co.searchwellington.filters;

import jakarta.servlet.http.HttpServletRequest;

public class RequestPath {

    public static String getPathFrom(HttpServletRequest request) {
        return request.getRequestURI();
    }

}
