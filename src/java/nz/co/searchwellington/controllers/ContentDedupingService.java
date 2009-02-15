package nz.co.searchwellington.controllers;

import java.util.List;

import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Website;

public class ContentDedupingService {

    public void dedupeIndexPageNewsitems(List<Newsitem> latestNewsitems, List<Newsitem> commentedNewsitems) { 
        latestNewsitems.removeAll(commentedNewsitems);        
    }

    
    public void dedupeTagPageNewsitems(List<Resource> newsitemsOnPage, List<Resource> commentedNewsitemOnPage, List<Website> taggedWebsites) {
        boolean isSingleColumnLayout = !taggedWebsites.isEmpty();
        if (isSingleColumnLayout) {
            newsitemsOnPage.removeAll(commentedNewsitemOnPage);
        }
    }
    
}
