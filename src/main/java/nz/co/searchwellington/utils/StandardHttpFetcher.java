package nz.co.searchwellington.utils;

import com.google.common.base.Strings;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class StandardHttpFetcher implements HttpFetcher {    // TODO migrate to common http

    private static Logger log = Logger.getLogger(StandardHttpFetcher.class);

    private static final int HTTP_TIMEOUT = 60000;

    private final String userAgent;
    private final String httpProxyHostname;
    private final Integer httpProxyPort;

    @Autowired
    public StandardHttpFetcher(
            @Value("#{config['http.useragent']}") String userAgent,
            @Value("#{config['http.proxy.hostname']}") String httpProxyHostname,
            @Value("#{config['http.proxy.port']}") Integer httpProxyPort) {
        this.userAgent = userAgent;
        this.httpProxyHostname = httpProxyHostname;
        this.httpProxyPort = httpProxyPort;
    }

    public HttpFetchResult httpFetch(String url) {
        log.info("Attempting fetch of url: " + url + " using " + this.getClass().getCanonicalName());
        HttpClient client = setupClient();
        try {
            HttpMethod method = new GetMethod(url);
            method.addRequestHeader("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
            method.addRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            client.executeMethod(method);

            log.info("http status was: " + method.getStatusCode());
            if (method.getStatusCode() == HttpStatus.SC_OK) {
                InputStream stream = method.getResponseBodyAsStream();
                return new HttpFetchResult(method.getStatusCode(), stream);
            }
            return new HttpFetchResult(method.getStatusCode(), null);

        } catch (HttpException e) {
            log.warn("An exception was thrown will trying to http fetch " + url + "; see debug log level");
            log.info(e);

        } catch (IOException e) {
            log.warn("An exception was thrown will trying to http fetch; see debug log level");
            log.debug(e);
        }

        log.info("Setting -1 status code for: " + url);
        return new HttpFetchResult(-1, null);
    }

    public String getUserAgent() {
        return userAgent;
    }


    private HttpClient setupClient() {
        final HttpClient client = new HttpClient();
        if (!Strings.isNullOrEmpty(userAgent)) {
            log.info("Setting http client user agent to: " + userAgent);
            client.getParams().setParameter(HttpClientParams.USER_AGENT, userAgent);
        }

        if (!Strings.isNullOrEmpty(httpProxyHostname) && httpProxyPort != null) {
            log.info("Setting http proxy to: " + httpProxyHostname + ":" + httpProxyPort);
            client.getHostConfiguration().setProxy(httpProxyHostname, httpProxyPort);
        }

        client.getParams().setParameter("http.socket.timeout", new Integer(HTTP_TIMEOUT));
        client.getParams().setParameter("http.connection.timeout", new Integer(HTTP_TIMEOUT));
        client.getParams().setParameter("http.protocol.content-charset", "UTF-8");
        return client;
    }

}
