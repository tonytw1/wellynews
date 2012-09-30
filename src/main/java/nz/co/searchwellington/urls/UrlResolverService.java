package nz.co.searchwellington.urls;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UrlResolverService {	// TODO move to common

	private static Logger log = Logger.getLogger(UrlResolverService.class);

	protected RedirectingUrlResolver[] redirectResolvers;

	@Autowired
	public UrlResolverService(RedirectingUrlResolver... redirectResolvers) {
		this.redirectResolvers = redirectResolvers;
	}
	
	public String resolveUrl(String url) {
		return fullyResolveUrl(url, 0);		
	}
	
	private String fullyResolveUrl(String url, int depth) {
		depth = depth + 1;
		while (isResolvable(url) && depth < 5) {
			String resolvedUrl = resolveSingleUrl(url);
			if (!resolvedUrl.equals(url)) {
				return fullyResolveUrl(resolvedUrl, depth);
			}
			return resolvedUrl;
		}
		return url;
	}
	
	protected boolean isResolvable(String url) {
		for (RedirectingUrlResolver resolver : redirectResolvers) {
			if (resolver.isValid(url)) {
				return true;
			}
		}
		return false;
	}
	
	protected String resolveSingleUrl(String url) {
		for (RedirectingUrlResolver resolver : redirectResolvers) {
			if (resolver.isValid(url)) {
				String resolvedUrl = resolver.resolveUrl(url);
				if (resolvedUrl != null) {
					log.info("Redirected url '" + url + "' resolved to: " + resolvedUrl);
					url = resolvedUrl;
				} else {
					log.warn("Failed to resolve redirected url: " + url);
				}
			}
		}
		return url;
	}

}
