package nz.co.searchwellington.repositories.mongo;

import nz.co.searchwellington.model.Snapshot;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.code.morphia.Datastore;

@Component
public class MongoSnapshotDAO {
	
	private static Logger log = Logger.getLogger(MongoSnapshotDAO.class);

	private DataStoreFactory dataStoreFactory;
	
	@Autowired
	public MongoSnapshotDAO(DataStoreFactory dataStoreFactory) {
		this.dataStoreFactory = dataStoreFactory;
	}
	
	public Snapshot getLatestFor(String url) {
		final Datastore ds = dataStoreFactory.getDs();
		if (ds != null) {		
			try {
				return ds.find(Snapshot.class, "url", url).order("-date").get();
			} catch (Exception e) {
				log.error(e);
			}
		} else {
			log.warn("Mongo datastore is null; returning null");
		}
		return null;
	}

	public void put(Snapshot snapshot) {
		final Datastore ds = dataStoreFactory.getDs();
		if (ds != null) {
			try {
				ds.save(snapshot);
			} catch (Exception e) {
				log.error(e);
			}
		} else {
			log.warn("Mongo datastore is null; returning null");
		}
	}
	
	public void evict(String url) {
		// TODO Auto-generated method stub		
	}

}
