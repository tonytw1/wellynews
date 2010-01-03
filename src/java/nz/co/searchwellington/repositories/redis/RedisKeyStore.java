package nz.co.searchwellington.repositories.redis;

import java.util.List;

import org.apache.log4j.Logger;
import org.jredis.RedisException;
import org.jredis.ri.alphazero.JRedisService;

public class RedisKeyStore {

	private static Logger log = Logger.getLogger(RedisKeyStore.class);
	
	private JRedisService jredis;
	private String keyPrefix;	
	private String redisHostname;
	private int redisPort;


	public RedisKeyStore() {
	}


	public String get(String id) {
		final String key = generateKey(id);

		try {
			connect();			
			if (jredis.exists(key)) {
				return new String(jredis.get(key));
			}

		} catch (Exception e) {
			log.warn("A Redis exception occured while trying to fetch for key '" + key + "': ", e);
		}
		return null;
	}

	
	public void put(String id, String content) {
		final String key = generateKey(id);
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
			log.warn("A Redis exception occured while trying to set for key '"
					+ key + "': ", e);
		}
	}

	
	public int size() {
		List<String> keys;
		try {
			keys = jredis.keys(keyPrefix + "*");
			if (keys != null) {
				return keys.size();
			}
		} catch (RedisException e) {
			log.warn("A Redis exception occured while trying to size: '"
					+ keyPrefix, e);
		}
		return 0;
	}

	
	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}

	public void setRedisHostname(String redisHostname) {
		this.redisHostname = redisHostname;
	}

	public void setRedisPort(int redisPort) {
		this.redisPort = redisPort;
	}

	private void connect() throws RedisException {
		if (jredis == null) {
			jredis = new JRedisService(redisHostname, redisPort);			
		}
	}

	private String generateKey(String id) {
		return keyPrefix + id;
	}

}
