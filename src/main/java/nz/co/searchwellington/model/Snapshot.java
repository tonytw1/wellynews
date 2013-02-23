package nz.co.searchwellington.model;

import java.util.Date;

public class Snapshot {
	
	private String url;
	private Date date;
	private String body;

	public Snapshot() {
	}

	public Snapshot(String url, Date date, String body) {
		this.url = url;
		this.date = date;
		this.body = body;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return "Snapshot [body=" + body + ", date=" + date + ", url=" + url + "]";
	}

}
