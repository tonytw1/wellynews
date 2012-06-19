package nz.co.searchwellington.views;

import org.apache.commons.codec.digest.DigestUtils;

public class EtagGenerator {

	public String makeEtagFor(String data) {
		return DigestUtils.md5Hex(data);
	}
	
}
