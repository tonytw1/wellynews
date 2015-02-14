package nz.co.searchwellington.model.frontend;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

public class FrontendImageTest {

	@Test
	public void canSerializeForCaching() throws Exception {
		FrontendImage frontendImage = new FrontendImage();
		new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(frontendImage);
	}

}
