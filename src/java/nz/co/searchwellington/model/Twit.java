package nz.co.searchwellington.model;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import net.unto.twitter.TwitterProtos.Status;

public class Twit {
	
	private int id;
	private Long twitterid;	
	private String text;
	private String author;
	private String profileImage;	// TODO split off
	private Date date;


	public Twit() {		
	}

	
	public Twit(Status status) {	
		this.twitterid = status.getId();
		this.text = status.getText();
		this.author = status.getUser().getScreenName();
		this.profileImage = status.getUser().getProfile().getImageUrl();
		
		DateTimeFormatter parser = DateTimeFormat.forPattern("E MMM dd HH:mm:ss Z YYYY");
		DateTime time = parser.parseDateTime(status.getCreatedAt());
		this.date = time.toDate();
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


	

	public String getProfileImage() {
		return profileImage;
	}


	public void setProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}


	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	
	
		
	
	// TODO equals needs to look at TwitterID - set that as hibernate id?
	
	
}
