package nz.co.searchwellington.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SupressionService {

	private SupressionDAO suppressionDAO;
	
	public SupressionService() {
	}
	
	@Autowired
	public SupressionService(SupressionDAO suppressionDAO) {		
		this.suppressionDAO = suppressionDAO;
	}
	
	@Transactional
	public void suppressUrl(String urlToSupress) {		
		if (!suppressionDAO.isSupressed(urlToSupress)) {
			suppressionDAO.addSuppression(urlToSupress);
		}		
	}
	
	@Transactional
	public void unsupressUrl(String url) {
		suppressionDAO.removeSupressionForUrl(url);
	}

}
