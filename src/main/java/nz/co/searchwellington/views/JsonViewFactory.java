package nz.co.searchwellington.views;

import org.springframework.web.servlet.View;

public class JsonViewFactory {

	public View makeView() {
		return new JSONView();
	}
	
}
