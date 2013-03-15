package nz.co.searchwellington.geocoding.osm;

import java.util.List;

import nz.co.searchwellington.model.OsmId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.common.geo.model.Place;

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
	public List<Place> resolveAddress(String address) {
		return cachingNominatimResolveAddressService.callService(address);
	}
	
	@Override
	public Place resolveOsmId(OsmId osmId) {
		return cachingNominatimResolveOsmIdService.callService(osmId);
	}
	
}
