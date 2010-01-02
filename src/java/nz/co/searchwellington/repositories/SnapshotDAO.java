package nz.co.searchwellington.repositories;

import java.util.List;

import org.apache.log4j.Logger;
import org.jredis.JRedis;
import org.jredis.RedisException;
import org.jredis.ri.alphazero.JRedisClient;

public class SnapshotDAO {
		
    private static final String SNAPSHOT_URL_PREFIX = "url-snapshots::";

	Logger log = Logger.getLogger(SnapshotDAO.class);
    	
    private  JRedis jredis;
    
    private String redisHostname;
    private int redisPort;
    
	
    public SnapshotDAO() {
	}
	
        
	public String loadContentForUrl(String url) {
		final String key = generateKey(url);			
		try {
			connect();
			logoutSize();			
			
			if (jredis.exists(key)) {
				return new String(jredis.get(key));
			}
			
		} catch (Exception e) {
			log.warn("A Redis exception occured while trying to fetch for key '" + key + "': ", e);
		}
        return null;
	}

	
	public void setSnapshotContentForUrl(String url, String content) {
		final String key = generateKey(url);
		try {			
			connect();
			log.info("Setting snapshot for key: " + key);
			if (content != null) {
				jredis.set(key, content);
			} else {
				log.info("Content for key '" + key + "' was null; deleting");
				jredis.del(key);
			}
			
		} catch (Exception e) {
			log.warn("A Redis exception occured while trying to set for key '" + key + "': ", e);
		}
	}
	
		
	
	
	public void setRedisHostname(String redisHostname) {
		this.redisHostname = redisHostname;
	}


	public void setRedisPort(int redisPort) {
		this.redisPort = redisPort;
	}


	private void connect() throws RedisException {
		if (jredis == null) {
			jredis = new JRedisClient(redisHostname, redisPort);
		}
		jredis.ping();
	}


	private void logoutSize() throws RedisException {
		List<String> keys = jredis.keys(SNAPSHOT_URL_PREFIX + "*");
		if (keys != null) {
			log.info("Snapshot cache contains: " + keys.size() + " items");	// TODO expensive - move to admin controller invoke
		}
	}


	private String generateKey(String url) {
		return SNAPSHOT_URL_PREFIX + url;
	}
	
	
	
}
