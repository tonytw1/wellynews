package nz.co.searchwellington.geocoding.osm;

import nz.co.searchwellington.model.OsmId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import uk.co.eelpieconsulting.common.caching.CachingServiceWrapper;
import uk.co.eelpieconsulting.common.caching.MemcachedCache;
import uk.co.eelpieconsulting.common.geo.Place;

@Controller
public class CachingNominatimResolveOsmIdService extends CachingServiceWrapper<OsmId, Place> {

	@Autowired
	public CachingNominatimResolveOsmIdService(NominatimResolveOsmIdCachingServiceWrapper service, MemcachedCache cache) {
		super(service, cache);
	}

}
