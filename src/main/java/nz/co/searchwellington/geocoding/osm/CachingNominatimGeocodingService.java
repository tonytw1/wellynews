package nz.co.searchwellington.geocoding.osm;

import java.util.List;

import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.OsmId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CachingNominatimGeocodingService implements GeoCodeService {
	
	private CachingNominatimResolveAddressService cachingNominatimResolveAddressService;
	private CachingNominatimResolveOsmIdService cachingNominatimResolveOsmIdService;
	
	@Autowired
	public CachingNominatimGeocodingService(CachingNominatimResolveAddressService cachingNominatimResolveAddressService, CachingNominatimResolveOsmIdService cachingNominatimResolveOsmIdService) {
		this.cachingNominatimResolveAddressService = cachingNominatimResolveAddressService;
		this.cachingNominatimResolveOsmIdService = cachingNominatimResolveOsmIdService;
	}

	@Override
	public List<Geocode> resolveAddress(String address) {
		return cachingNominatimResolveAddressService.callService(address);
	}
	
	@Override
	public Geocode resolveOsmId(OsmId osmId) {
		return cachingNominatimResolveOsmIdService.callService(osmId);
	}
	
}
