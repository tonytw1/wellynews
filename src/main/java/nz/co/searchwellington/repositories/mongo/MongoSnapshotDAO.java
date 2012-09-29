package nz.co.searchwellington.repositories.mongo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import nz.co.searchwellington.model.Snapshot;

@Component
public class MongoSnapshotDAO {
	
	private DataStoreFactory dataStoreFactory;
	
	@Autowired
	public MongoSnapshotDAO(DataStoreFactory dataStoreFactory) {
		this.dataStoreFactory = dataStoreFactory;
	}
	
	public Snapshot getLatestFor(String url) {	
		return dataStoreFactory.getDs().find(Snapshot.class, "url", url).order("-date").get();
	}

	public void put(Snapshot snapshot) {
		dataStoreFactory.getDs().save(snapshot);
	}

	public void evict(String url) {
		// TODO Auto-generated method stub		
	}

}
