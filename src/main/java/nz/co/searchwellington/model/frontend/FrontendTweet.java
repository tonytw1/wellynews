package nz.co.searchwellington.model.frontend;

public class FrontendTweet {

	private String text, author;

	public FrontendTweet() {
	}
	
	public FrontendTweet(String text, String author) {
		this.text = text;
		this.author = author;
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	@Override
	public String toString() {
		return "FrontendTweet [author=" + author + ", text=" + text + "]";
	}
	
}
