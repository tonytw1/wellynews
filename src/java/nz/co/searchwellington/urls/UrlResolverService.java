package nz.co.searchwellington.urls;

import org.apache.log4j.Logger;

public class UrlResolverService {

	Logger log = Logger.getLogger(UrlResolverService.class);

	protected RedirectingUrlResolver[] redirectResolvers;

	public UrlResolverService(RedirectingUrlResolver... redirectResolvers) {
		this.redirectResolvers = redirectResolvers;
	}

	
	public String resolveUrl(String url) {
		while (isResolvable(url)) {
			String resolvedUrl = resolveSingleUrl(url);
			if (!resolvedUrl.equals(url)) {
				return resolveUrl(resolvedUrl);
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
