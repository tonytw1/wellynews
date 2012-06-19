package nz.co.searchwellington.views;

import org.springframework.web.servlet.View;

public class JsonViewFactory {
	
	private JsonSerializer jsonSerializer;
	private EtagGenerator etagGenerator;
	
	public JsonViewFactory(JsonSerializer jsonSerializer, EtagGenerator etagGenerator) {
		this.jsonSerializer = jsonSerializer;
		this.etagGenerator = etagGenerator;
	}
	
	public View makeView() {
		return new JsonView(jsonSerializer, etagGenerator);
	}
	
}
