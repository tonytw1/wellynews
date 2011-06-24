package nz.co.searchwellington.views;

import java.util.List;

import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.repositories.ContentRetrievalService;

@Deprecated
// TODO and what does this do again?
public class ContentDataSource {

	private ContentRetrievalService contentRetrievalService;

	public ContentDataSource(ContentRetrievalService contentRetrievalService) {
		this.contentRetrievalService = contentRetrievalService;
	}

	public List<FrontendResource> getLatestNewsitems() {
		return contentRetrievalService.getLatestNewsitems(5);
	}

	public List<FrontendResource> getPublisherTagNewsitems(String publisherUrlWords, String tagName) {
		return contentRetrievalService.getPublisherTagCombinerNewsitems(publisherUrlWords, tagName, 10);
	}

}
