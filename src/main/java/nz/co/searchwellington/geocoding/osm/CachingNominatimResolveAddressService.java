package nz.co.searchwellington.geocoding.osm;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.caching.CachingServiceWrapper;
import uk.co.eelpieconsulting.common.caching.MemcachedCache;
import uk.co.eelpieconsulting.common.geo.Place;

@Component
public class CachingNominatimResolveAddressService extends CachingServiceWrapper<String, List<Place>> {

	@Autowired
	public CachingNominatimResolveAddressService(NominatimResolveAddressCachingServiceWrapper service, MemcachedCache cache) {
		super(service, cache);
	}

}
