package nz.co.searchwellington.repositories.mongo;

import java.net.UnknownHostException;

import nz.co.searchwellington.model.Snapshot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

@Component
public class DataStoreFactory {

	private Datastore ds;
	
    @Value("#{config['mongo.hostname']}")
	private String hostname;
	
	public DataStoreFactory() {
		Morphia morphia = new Morphia();
		morphia.map(Snapshot.class);
		Mongo m;
		try {
			m = new Mongo(hostname);
			ds = morphia.createDatastore(m, "searchwellington");			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
	public Datastore getDs() {
		return ds;
	}
	
}
