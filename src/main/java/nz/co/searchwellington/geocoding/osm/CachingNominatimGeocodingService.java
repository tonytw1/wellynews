package nz.co.searchwellington.geocoding.osm;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import nz.co.searchwellington.geocoding.CachingServiceWrapper;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.OsmId;

@Component
public class CachingNominatimGeocodingService implements GeoCodeService {
	
	private CachingServiceWrapper<String, List<Geocode>> resolveAddressCachingWrapper;
	private CachingServiceWrapper<OsmId, Geocode> resolveOsmIdCachingWrapper;
	
	@Autowired
	public CachingNominatimGeocodingService(CachingServiceWrapper<String, List<Geocode>> resolveAddressCachingWrapper,
			CachingServiceWrapper<OsmId, Geocode> resolveOsmIdCachingWrapper) {
		this.resolveAddressCachingWrapper = resolveAddressCachingWrapper;
		this.resolveOsmIdCachingWrapper = resolveOsmIdCachingWrapper;
	}

	@Override
	public List<Geocode> resolveAddress(String address) {
		return resolveAddressCachingWrapper.callService(address);
	}

	@Override
	public Geocode resolveOsmId(OsmId osmId) {
		return resolveOsmIdCachingWrapper.callService(osmId);
	}
	
}
