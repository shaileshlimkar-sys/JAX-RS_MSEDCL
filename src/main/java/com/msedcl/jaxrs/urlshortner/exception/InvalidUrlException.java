package com.msedcl.jaxrs.urlshortner.exception;

public class InvalidUrlException extends RuntimeException {


	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3464811124494766036L;

	public InvalidUrlException(String message) {
		super(message);
	}

}