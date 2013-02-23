package nz.co.searchwellington.repositories.mongo;

import nz.co.searchwellington.model.Snapshot;

public interface SnapshotArchive {

	Snapshot getLatestFor(String url);
	void put(Snapshot snapshot);

}
