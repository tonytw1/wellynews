package nz.co.searchwellington.repositories;

import org.apache.log4j.Logger;
import org.xurble.technorati.Cosmos;
import org.xurble.technorati.Technorati;

public class TechnoratiDAO {
        
    private String technoratiKey;
    private final int LINK_STRIP_LIMIT = 20;
    

    Logger log = Logger.getLogger(TechnoratiDAO.class); 
    

    
    public TechnoratiDAO(String technoratiKey) {
        super();
        this.technoratiKey = technoratiKey;
    }
    
    
        
    public int getTechnoratiLinkCount (String url, String excludeURL) {
        
        // Fetch the cosmos for this url.
        Cosmos cosmos = fetchTecnoratiCosmos(url);        
        if (cosmos != null) {
            return extractFilteredLinkCountFromCosmos(cosmos, excludeURL);       
        } else {
           log.warn("Failed to fetch Technorati Cosmos. Cosmos was null");
           return 0;
        }       
    }

    
    
    
    
    private Cosmos fetchTecnoratiCosmos (String url) {        
        Technorati api = new Technorati(technoratiKey);
        Cosmos cosmos = api.getCosmos(url, Technorati.COSMOS_TYPE_LINK);
        return cosmos;
    }
    
    
    
    
    
    /** 
     * Given a retrieved Technorati Cosmos for a given url and the URL 
     * of our own site return a link count. 
     * We are trying to mask the effect of our own links to resources appearing i
     * in the Technorati link count.
     * 
     * @param cosmos
     * @param excludeSiteUrl
     * @return
     */
    private int extractFilteredLinkCountFromCosmos (Cosmos cosmos, String excludeSiteUrl) {
    
        int linkCount = cosmos.getInboundLinks();   
        
        // If a URL has a small number of links (less than 20 per Cosmos page), 
        // then alot of those links could be from us. Strip them out to
        // try and lower the instance of loopbacks just to ourselves.
        if ((linkCount <= LINK_STRIP_LIMIT) && (excludeSiteUrl != null)){
            log.info("Cosmos has less than " + LINK_STRIP_LIMIT + " links.");
            log.info("Attempting to strip links to blog url: " + excludeSiteUrl);
            
            int strippedLinks =0;
            for (int i=0; i < cosmos.numItems(); i++) {
                String blogUrl = cosmos.getItem(i).getWebLog().getURL();
                log.info("Link entire has blog url: " + blogUrl);               
                if (!blogUrl.equals(excludeSiteUrl)) {
                    strippedLinks++;
                }
            }
            log.info("Stripped link count is: " + strippedLinks);
            linkCount = strippedLinks;
        }
                
        return linkCount;
    }
    
    
}
