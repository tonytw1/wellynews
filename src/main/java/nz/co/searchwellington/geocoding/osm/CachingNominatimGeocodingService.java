package nz.co.searchwellington.geocoding.osm;

import java.util.List;

import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.OsmId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CachingNominatimGeocodingService implements GeoCodeService {
	
	private NominatimResolveAddressCachingServiceWrapper resolveAddressCachingWrapper;
	private NominatimResolveOsmIdCachingServiceWrapper resolveOsmIdCachingWrapper;
	
	@Autowired
	public CachingNominatimGeocodingService(NominatimResolveAddressCachingServiceWrapper resolveAddressCachingWrapper, NominatimResolveOsmIdCachingServiceWrapper resolveOsmIdCachingWrapper) {
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
