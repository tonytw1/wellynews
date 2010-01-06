package nz.co.searchwellington.repositories.redis;

import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

import tokyotyrant.RDB;

public class KeyStore {

	private static Logger log = Logger.getLogger(KeyStore.class);
	
	private RDB db;
	
	private String keyPrefix;	
	private String hostname;
	private int port;


	public KeyStore() {
	}


	public synchronized String get(String id) {
		final String key = generateKey(id);
		try {
			log.info("Getting content for key: " + key);
			connect();			
			if (db.get(key) != null) {
				return (String) db.get(key);
			}
			
		} catch (Exception e) {
			log.warn("An xception occured while trying to fetch for key '" + key + "': ", e);
		}
		return null;
	}

	
	public synchronized void put(String id, String content) {
		connect();
		if (content != null) {
			final String key = generateKey(id);
			log.info("Setting snapshot for key: " + key);
			connect();
			db.put(key, content);			
		} else {
			log.warn("Content is null for id; removing: " + id);
			this.evict(id);
		}
		
	}

	public void evict(String id) {
		connect();
		final String key = generateKey(id);
		log.info("Evicting key: " + key);
		db.out(key);
	}
	
	
	public long size() {		
		return db.rnum();
	}

	private void connect() {		
		if (db == null) {
			db = new RDB();
			db.open(new InetSocketAddress(hostname, port));
		}
		log.info("Keystore contains: " + Long.toString(this.size()));
	}

	
	private String generateKey(String id) {
		return keyPrefix + id;
	}
	
	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public void setPort(int port) {
		this.port = port;
	}


}
