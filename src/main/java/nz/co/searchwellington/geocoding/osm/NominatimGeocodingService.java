package nz.co.searchwellington.geocoding.osm;

import java.io.IOException;
import java.util.List;

import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.OsmId;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import fr.dudie.nominatim.client.JsonNominatimClient;
import fr.dudie.nominatim.client.NominatimClient;
import fr.dudie.nominatim.model.Address;

@Component
public class NominatimGeocodingService implements GeoCodeService {
	
	private static Logger log = Logger.getLogger(NominatimGeocodingService.class);

	private static final String NOMINATIM_USER = "tony@wellington.gen.nz";
	
	public List<Geocode> resolveAddress(String address) {
		log.info("Resolving address with Nominatim: " + address);
		final NominatimClient nominatimClient = getNominatimClient();
		try {
			List<Address> results = nominatimClient.search(address);
			if (!results.isEmpty()) {
				List<Geocode> geocodes = Lists.newArrayList();
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
	
	public Geocode resolveOsmId(OsmId osmId) {
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
	
	private Geocode buildGeocodeFor(Address result) {
		return new Geocode(result.getDisplayName(), result.getLatitude(), result.getLongitude(), result.getElementType(), Long.parseLong(result.getOsmId()), result.getOsmType(), "OSM");
	}
	
}
