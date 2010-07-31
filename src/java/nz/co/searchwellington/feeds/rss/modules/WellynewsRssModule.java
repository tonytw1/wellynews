package nz.co.searchwellington.feeds.rss.modules;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.feed.module.ModuleImpl;

public class WellynewsRssModule extends  ModuleImpl implements Module {

	private static final long serialVersionUID = 1L;
	static final String URI = "http://wellington.gen.nz/api/rssmodule";
	
	private String commented;
	
	public WellynewsRssModule() {
		super(WellynewsRssModule.class, WellynewsRssModule.URI);
	}
	
	@Override
	public String getUri() {
		return URI;
	}

	@Override
	public void copyFrom(Object obj) {
		WellynewsRssModule module = (WellynewsRssModule) obj;	
	}

	@Override
	public Class getInterface() {
		return WellynewsRssModule.class;
	}

	public String getCommented() {
		return commented;
	}

	public void setCommented(String commented) {
		this.commented = commented;
	}
	
}
