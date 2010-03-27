package nz.co.searchwellington.repositories;

import java.util.Date;
import java.util.List;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Suggestion;

public interface SuggestionRepository {

	public Suggestion createSuggestion(Feed feed, String url, Date firstSeen);

	public void addSuggestion(Suggestion suggestion);

	public boolean isSuggested(String url);

	public List<Suggestion> getAllSuggestions();

	public List<Suggestion> getSuggestions(int maxResults);

	public void removeSuggestion(String url);

}
