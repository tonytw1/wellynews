package nz.co.searchwellington.geocoding.osm;

import java.util.List;

import nz.co.searchwellington.caching.MemcachedCache;
import nz.co.searchwellington.geocoding.CachingServiceWrapper;
import nz.co.searchwellington.model.Geocode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CachingNominatimResolveAddressService extends CachingServiceWrapper<String, List<Geocode>> {

	@Autowired
	public CachingNominatimResolveAddressService(NominatimResolveAddressCachingServiceWrapper service, MemcachedCache cache) {
		super(service, cache);
	}

}
