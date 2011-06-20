package nz.co.searchwellington.model;

@Deprecated
public class SolrHydratedNewsitemImpl extends NewsitemImpl {

	private String explictPublisherName;
	
	public SolrHydratedNewsitemImpl(String explictPublisherName) {
		this.explictPublisherName = explictPublisherName;
	}

	@Override
	public String getPublisherName() {
		return explictPublisherName;
	}
	
}
