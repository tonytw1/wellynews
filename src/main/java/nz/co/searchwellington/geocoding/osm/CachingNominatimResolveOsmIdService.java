package nz.co.searchwellington.geocoding.osm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import nz.co.searchwellington.caching.MemcachedCache;
import nz.co.searchwellington.geocoding.CachingServiceWrapper;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.OsmId;

@Controller
public class CachingNominatimResolveOsmIdService extends CachingServiceWrapper<OsmId, Geocode> {

	@Autowired
	public CachingNominatimResolveOsmIdService(NominatimResolveOsmIdCachingServiceWrapper service, MemcachedCache cache) {
		super(service, cache);
	}

}
