package nz.co.searchwellington.repositories.mongo;

import java.net.UnknownHostException;

import nz.co.searchwellington.model.Snapshot;

import org.springframework.stereotype.Component;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

@Component
public class DataStoreFactory {

	private Datastore ds;
	
	private String hostname;
	
	public DataStoreFactory() throws UnknownHostException, MongoException {
		Morphia morphia = new Morphia();
		morphia.map(Snapshot.class);
		Mongo m = new Mongo(hostname);
		ds = morphia.createDatastore(m, "searchwellington");
	}
	
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
	public Datastore getDs() {
		return ds;
	}
	
}
