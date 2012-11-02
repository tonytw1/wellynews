package nz.co.searchwellington.feeds;

import java.util.List;

import nz.co.searchwellington.model.FeedNewsitem;
import nz.co.searchwellington.model.FrontendFeedNewsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.repositories.HibernateResourceDAO;
import nz.co.searchwellington.repositories.SupressionDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class FeedItemLocalCopyDecorator {
	
	private HibernateResourceDAO resourceDAO;
	private SupressionDAO suppressionDAO;
	
	@Autowired
	public FeedItemLocalCopyDecorator(HibernateResourceDAO resourceDAO, SupressionDAO suppressionDAO) {
		this.resourceDAO = resourceDAO;
		this.suppressionDAO = suppressionDAO;
	}
	
	public List<FrontendFeedNewsitem> addSupressionAndLocalCopyInformation(List<FeedNewsitem> feedNewsitems) {
		final List<FrontendFeedNewsitem> decoratedFeednewsitems = Lists.newArrayList();
		for (FeedNewsitem feedNewsitem : feedNewsitems) {

			FrontendFeedNewsitem frontendFeedNewsitem = new FrontendFeedNewsitem(feedNewsitem);
			if (feedNewsitem.getUrl() != null) {
				
				Resource localCopy = resourceDAO.loadResourceByUrl(feedNewsitem.getUrl());	// TODO expensive?
				if (localCopy != null) {
					frontendFeedNewsitem.setLocalCopy(localCopy.getId());
				}
				boolean isSuppressed = suppressionDAO.isSupressed(feedNewsitem.getUrl());					
				frontendFeedNewsitem.setSuppressed(isSuppressed);						
			}
			decoratedFeednewsitems.add(frontendFeedNewsitem);
		}
		return decoratedFeednewsitems;
	}
	

}
