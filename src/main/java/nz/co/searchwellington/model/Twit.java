package nz.co.searchwellington.model;

import java.util.Date;

import org.joda.time.DateTime;

import twitter4j.Status;


public class Twit {
	
	private int id;
	private Long twitterid;
	private long inReplyToStatusId;
	private String text;
	private String author;
	private Date date;


	public Twit() {		
	}
	
	public Twit(Status status) {
		this.twitterid = status.getId();
		this.inReplyToStatusId = status.getInReplyToStatusId();
		this.text = status.getText();
		this.author = status.getUser().getScreenName();
		DateTime time = new DateTime(status.getCreatedAt());
		this.date = time.toDate();
	}
	
	public Twit(String author, String text) {
		this.author = author;
		this.text = text;
	}

	public int getId() {
		return id;
	}



	public void setId(int id) {
		this.id = id;
	}



	public Long getTwitterid() {
		return twitterid;
	}



	public void setTwitterid(Long twitterid) {
		this.twitterid = twitterid;
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
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	
	public long getInReplyToStatusId() {
		return inReplyToStatusId;
	}

	public void setInReplyToStatusId(long inReplyToStatusId) {
		this.inReplyToStatusId = inReplyToStatusId;
	}
	
	// TODO equals needs to look at TwitterID - set that as hibernate id?
	
}
