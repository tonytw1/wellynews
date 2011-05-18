package nz.co.searchwellington.repositories;

import nz.co.searchwellington.repositories.keystore.KeyStore;

public class SnapshotDAO {
		
	private KeyStore keystore;
	private String keyPrefix;
	
	public SnapshotDAO(KeyStore snapshotsCache) {
		this.keystore = snapshotsCache;
	}
	
	public void setSnapshotContentForUrl(String url, String content) {
		keystore.put(generateKey(url), content);
	}
	
	public String loadContentForUrl(String url) {
		return keystore.get(generateKey(url));
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
