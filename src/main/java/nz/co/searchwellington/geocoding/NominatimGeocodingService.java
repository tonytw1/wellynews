package nz.co.searchwellington.geocoding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.OsmId;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.dudie.nominatim.client.JsonNominatimClient;
import fr.dudie.nominatim.client.NominatimClient;
import fr.dudie.nominatim.model.Address;

@Component
public class NominatimGeocodingService implements GeoCodeService, CachableService<OsmId, Geocode> {
	
	private static Logger log = Logger.getLogger(NominatimGeocodingService.class);

	private static final String OSM_ID_CACHE_PREFIX = "osmidgeocode:";
	private static final String NOMINATIM_USER = "tony@wellington.gen.nz";
	
	@Override
	public Geocode callService(OsmId osmId) {
		return resolveAddress(osmId);
	}

	@Override
	public String getCacheKeyFor(OsmId parameter) {
		return OSM_ID_CACHE_PREFIX + parameter.getId() + parameter.getType() + ":";
	}

	@Override
	public int getTTL() {
		return 60 * 1000 * 48;
	}
	
	@Override
	public List<Geocode> resolveAddress(String address) {
		log.info("Resolving address with Nominatim: " + address);
		final NominatimClient nominatimClient = getNominatimClient();
		try {
			List<Address> results = nominatimClient.search(address);
			if (!results.isEmpty()) {
				List<Geocode> geocodes = new ArrayList<Geocode>();
				for (Address result : results) {
					log.info("Resolved to OSM place id #" + result.getOsmId() + "/" + result.getOsmType() + ": " + result.getDisplayName() + " (" + result.getElementType() + ")");
					geocodes.add(buildGeocodeFor(result));
				}
				return geocodes;
			}
			
		} catch (IOException e) {
			log.warn("Exception while searching for '" + address + "': " + e.getMessage());
		}		
		return null;
	}
	
	public Geocode resolveAddress(OsmId osmId) {
		log.info("Resolving OSM id with Nominatim: " + osmId);
		try {
			final NominatimClient nominatimClient = getNominatimClient();
			Address address = nominatimClient.getAddress(osmId.getType(), osmId.getId());
			return address != null ? buildGeocodeFor(address) : null;
			
		} catch (IOException e) {
			log.error(e);
			return null;
		}
	}
	
	private NominatimClient getNominatimClient() {
		HttpClient httpClient = new DefaultHttpClient();
		NominatimClient nominatimClient = new JsonNominatimClient(httpClient, NOMINATIM_USER);
		return nominatimClient;
	}

	private Geocode buildGeocodeFor(Address result) {	// TODO don't all for null resolves
		return new Geocode(result.getDisplayName(), result.getLatitude(), result.getLongitude(), result.getElementType(), Long.parseLong(result.getOsmId()), result.getOsmType(), "OSM");
	}
	
}
