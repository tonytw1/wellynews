package nz.co.searchwellington.urls;

import java.net.URLDecoder;

import org.apache.log4j.Logger;

public class UrlResolverService {

	Logger log = Logger.getLogger(UrlResolverService.class);

	protected RedirectingUrlResolver[] redirectResolvers;

	public UrlResolverService(RedirectingUrlResolver... redirectResolvers) {
		this.redirectResolvers = redirectResolvers;
	}

	
	public String resolveUrl(String url) {
		return fullyResolveUrl(url, 0);		
	}
	
	public String fullyResolveUrl(String url, int depth) {
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
					resolvedUrl = URLDecoder.decode(resolvedUrl);
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
