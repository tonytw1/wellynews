package nz.co.searchwellington.geocoding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nz.co.searchwellington.model.Geocode;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import fr.dudie.nominatim.client.JsonNominatimClient;
import fr.dudie.nominatim.client.NominatimClient;
import fr.dudie.nominatim.model.Address;

public class NominatimGeocodingService implements GeoCodeService {
	
	private static Logger log = Logger.getLogger(NominatimGeocodingService.class);

	@Override
	public List<Geocode> resolveAddress(String address) {
		log.info("Resolving address with Nominatim: " + address);
		HttpClient httpClient = new DefaultHttpClient();
		NominatimClient nominatimClient = new JsonNominatimClient(httpClient, "tony@wellington.gen.nz");
		try {
			List<Address> results = nominatimClient.search(address);
			if (!results.isEmpty()) {
				List<Geocode> geocodes = new ArrayList<Geocode>();
				for (Address result : results) {					
					log.info("Resolved to OSM place id #" + result.getPlaceId() + ": " + result.getDisplayName() + " (" + result.getElementType() + ")");
					geocodes.add(new Geocode(result.getDisplayName(), result.getLatitude(), result.getLongitude(), result.getElementType(), result.getPlaceId(), "OSM"));
				}
				return geocodes;
			}
			
		} catch (IOException e) {
			log.warn("Exception while searching for '" + address + "': " + e.getMessage());
		}		
		return null;
	}

}
