package nz.co.searchwellington.model;

@Deprecated
public class SolrHydratedNewsitemImpl extends NewsitemImpl {
	
	private static final long serialVersionUID = 1L;
	
	private String explictPublisherName;
	
	public SolrHydratedNewsitemImpl(String explictPublisherName) {
		this.explictPublisherName = explictPublisherName;
	}

	@Override
	public String getPublisherName() {
		return explictPublisherName;
	}
	
}
