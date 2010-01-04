package nz.co.searchwellington.repositories;

import nz.co.searchwellington.repositories.redis.KeyStore;

public class SnapshotDAO {
		
	private KeyStore snapshotsCache;

	public SnapshotDAO(KeyStore snapshotsCache) {
		this.snapshotsCache = snapshotsCache;
	}

	
	public void setSnapshotContentForUrl(String url, String content) {
		snapshotsCache.put(url, content);
	}
	
	public String loadContentForUrl(String url) {
		return snapshotsCache.get(url);
	}
					
}
