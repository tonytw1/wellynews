package nz.co.searchwellington.repositories;

import nz.co.searchwellington.model.Supression;

public class SupressionService {

	private SupressionDAO suppressionDAO;
	private SuggestionDAO suggestionDAO;
	
	public SupressionService(SupressionDAO suppressionDAO, SuggestionDAO suggestionDAO) {		
		this.suppressionDAO = suppressionDAO;
		this.suggestionDAO = suggestionDAO;
	}
	
	public void suppressUrl(String urlToSupress) {		
		if (!suppressionDAO.isSupressed(urlToSupress)) {
			suppressionDAO.addSupression(new Supression(urlToSupress));
          	suggestionDAO.removeSuggestion(urlToSupress);
		}		
	}

	public void unsupressUrl(String url) {
		suppressionDAO.removeSupressionForUrl(url);
	}

}
