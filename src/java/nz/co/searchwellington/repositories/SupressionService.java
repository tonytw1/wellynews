package nz.co.searchwellington.repositories;

import org.springframework.transaction.annotation.Transactional;


public class SupressionService {

	private SupressionRepository suppressionDAO;
	private SuggestionRepository suggestionDAO;
	
	
	public SupressionService() {
	}

	public SupressionService(SupressionRepository suppressionDAO, SuggestionRepository suggestionDAO) {		
		this.suppressionDAO = suppressionDAO;
		this.suggestionDAO = suggestionDAO;
	}
	
	
	@Transactional
	public void suppressUrl(String urlToSupress) {		
		if (!suppressionDAO.isSupressed(urlToSupress)) {
			suppressionDAO.addSuppression(urlToSupress);
          	suggestionDAO.removeSuggestion(urlToSupress);
		}		
	}

	
	@Transactional
	public void unsupressUrl(String url) {
		suppressionDAO.removeSupressionForUrl(url);
	}

}
