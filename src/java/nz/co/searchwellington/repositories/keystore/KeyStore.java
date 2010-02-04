package nz.co.searchwellington.repositories.keystore;

import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

import tokyotyrant.RDB;

public class KeyStore {

	private static Logger log = Logger.getLogger(KeyStore.class);
	
	private RDB db;
	
	private String hostname;
	private int port;


	public KeyStore() {
	}


	public synchronized String get(String id) {
		log.debug("Getting content for key: " + id);
		try {
			connect();			
			if (db != null && db.get(id) != null) {
				return (String) db.get(id);
			}			
		} catch (Exception e) {
			log.warn("An exception occured while trying to fetch for key '" + id);
		}
		return null;
	}

	
	public synchronized void put(String id, String content) {
		connect();
		if (content != null && db != null) {
			log.info("Setting snapshot for key: " + id);
			connect();
			db.put(id, content);			
		} else {
			log.warn("Content is null for id; removing: " + id);
			this.evict(id);
		}		
	}

	public void evict(String id) {
		connect();
		if (db != null) {
			try {
				log.info("Evicting key: " + id);
				db.out(id);
			} catch (Exception e) {
				return;
			}
		}
	}
	
	
	public long size() {
		connect();
		if (db != null) {
			try {
				return db.rnum();
			} catch (Exception e) {
				return 0;
			}
		}
		return 0;
	}

	
	private void connect() {		
		if (db == null) {
			db = new RDB();
			db.open(new InetSocketAddress(hostname, port));
		}		
	}
	
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
