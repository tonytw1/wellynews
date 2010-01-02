package nz.co.searchwellington.repositories;

import nz.co.searchwellington.repositories.redis.RedisKeyStore;

public class SnapshotDAO {
		
	private RedisKeyStore snapshotsCache;

	public SnapshotDAO(RedisKeyStore snapshotsCache) {
		this.snapshotsCache = snapshotsCache;
	}

	
	public void setSnapshotContentForUrl(String url, String content) {
		snapshotsCache.put(url, content);
	}
	
	public String loadContentForUrl(String url) {
		return snapshotsCache.get(url);
	}
					
}
