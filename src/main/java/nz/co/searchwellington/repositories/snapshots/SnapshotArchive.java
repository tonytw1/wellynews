package nz.co.searchwellington.repositories.snapshots;

import nz.co.searchwellington.model.Snapshot;

public interface SnapshotArchive {

	Snapshot getLatestFor(String url);
	void put(Snapshot snapshot);

}
