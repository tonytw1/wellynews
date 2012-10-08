package nz.co.searchwellington.geocoding.osm;

import java.util.List;

import nz.co.searchwellington.model.Geocode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.caching.CachingServiceWrapper;
import uk.co.eelpieconsulting.common.caching.MemcachedCache;

@Component
public class CachingNominatimResolveAddressService extends CachingServiceWrapper<String, List<Geocode>> {

	@Autowired
	public CachingNominatimResolveAddressService(NominatimResolveAddressCachingServiceWrapper service, MemcachedCache cache) {
		super(service, cache);
	}

}
