package nz.co.searchwellington.model;

import java.io.Serializable;
import java.util.Date;

import org.joda.time.DateTime;

public class Twit implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int id;
	private Long twitterid;
	private long inReplyToStatusId;
	private String text;
	private String author;
	private Date date;


	public Twit() {		
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
