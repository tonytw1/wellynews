package nz.co.searchwellington.model;

import nz.co.searchwellington.model.frontend.FrontendNewsitem;
import nz.co.searchwellington.model.frontend.FrontendNewsitemImpl;

public class FeedNewsitem extends FrontendNewsitemImpl implements FrontendNewsitem {
	
	private static final long serialVersionUID = 1L;
	
	private Image image;
	
	@Override
	public String getType() {
		return "FNI";
	}
	
	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}
	
}
