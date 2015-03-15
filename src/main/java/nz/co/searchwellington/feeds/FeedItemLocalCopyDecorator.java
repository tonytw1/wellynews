package nz.co.searchwellington.feeds;

import java.util.List;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.frontend.FeedNewsitemAcceptanceState;
import nz.co.searchwellington.model.frontend.FeedNewsitemForAcceptance;
import nz.co.searchwellington.model.frontend.FrontendFeedNewsitem;
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
	
	public List<FeedNewsitemForAcceptance> addSupressionAndLocalCopyInformation(List<FrontendFeedNewsitem> feedNewsitems) {
		final List<FeedNewsitemForAcceptance> decoratedFeednewsitems = Lists.newArrayList();
		for (FrontendFeedNewsitem feedNewsitem : feedNewsitems) {
            decoratedFeednewsitems.add(new FeedNewsitemForAcceptance(feedNewsitem, determineCurrentAcceptanceStateOf(feedNewsitem)));
		}
		return decoratedFeednewsitems;
	}

    private FeedNewsitemAcceptanceState determineCurrentAcceptanceStateOf(FrontendFeedNewsitem feedNewsitem) {
        Integer localCopyId = null;
        boolean isSuppressed = false;

        if (feedNewsitem.getUrl() != null) {
            Resource localCopy = resourceDAO.loadResourceByUrl(feedNewsitem.getUrl());	// TODO expensive?
            if (localCopy != null) {
                localCopyId = localCopy.getId();
            }
            isSuppressed = suppressionDAO.isSupressed(feedNewsitem.getUrl());
        }
        return new FeedNewsitemAcceptanceState(localCopyId, isSuppressed);
    }

}