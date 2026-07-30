package com.msedcl.jaxrs.urlshortner.model;

public class URLEntity {

	private String longUrl;
	private String shortUrl;
	public URLEntity() {
		
	}
	public URLEntity(String longUrl, String shortUrl) {
		super();
		this.longUrl = longUrl;
		this.shortUrl = shortUrl;
	}
	public String getLongUrl() {
		return longUrl;
	}
	public void setLongUrl(String longUrl) {
		this.longUrl = longUrl;
	}
	public String getShortUrl() {
		return shortUrl;
	}
	public void setShortUrl(String shortUrl) {
		this.shortUrl = shortUrl;
	}
	
}
