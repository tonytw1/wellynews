package nz.co.searchwellington.caching;

import java.io.IOException;

import org.apache.log4j.Logger;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;
import nz.co.searchwellington.controllers.TagController;

public class MemcachedCache {
	
	private static Logger log = Logger.getLogger(TagController.class);
	
	private MemcachedClient memcachedClient;

	private String memcachedUrls;
	
	public void setMemcachedUrls(String memcachedUrls) {
		this.memcachedUrls = memcachedUrls;
	}

	public void put(String key, int ttl, Object value) {
		try {
			memcachedClient = getClient();
			memcachedClient.add(key, ttl, value);			
		} catch (IOException e) {
			log.error("Failed to put to cache: " + e.getMessage());
		}		
	}

	public Object get(String key) {
		MemcachedClient memcachedClient;
		try {
			memcachedClient = getClient();
			return memcachedClient.get(key);
		} catch (IOException e) {
			log.error("Failed to get from cache: " + e.getMessage());
		}
		return null;
	}
	
	private MemcachedClient getClient() throws IOException {
		if (memcachedClient == null) {
			memcachedClient= new MemcachedClient( AddrUtil.getAddresses(memcachedUrls));
		}
		return memcachedClient;
	}
	
}
