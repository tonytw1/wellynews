package nz.co.searchwellington.repositories;

import java.util.Date;

import nz.co.searchwellington.repositories.keystore.KeyStore;

public class SnapshotDAO {
		
	private KeyStore keystore;
	private String keyPrefix;
	
	public SnapshotDAO(KeyStore snapshotsCache) {
		this.keystore = snapshotsCache;
	}
	
	public void setSnapshotContentForUrl(String url, Date date, String content) {
		keystore.put(generateKey(url), content);
	}
	
	public String loadLatestContentForUrl(String url) {
		return keystore.get(generateKey(url));
	}
	
	public String loadContentForUrl(String url, Date date) {
		return keystore.get(generateKey(url));		// TODO dates
	}

	public void evict(String url) {
		keystore.evict(generateKey(url));
	}
		
	private String generateKey(String id) {
		return keyPrefix + id;
	}
	
	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}

}
