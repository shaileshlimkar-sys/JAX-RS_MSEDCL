package com.msedcl.jaxrs.urlshortner.resource;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.msedcl.jaxrs.urlshortner.exception.InvalidShortKeyException;
import com.msedcl.jaxrs.urlshortner.exception.InvalidUrlException;
import com.msedcl.jaxrs.urlshortner.model.URLEntity;
import com.msedcl.jaxrs.urlshortner.service.UrlService;

@Path("/")
public class URLResorce {

	UrlService urlService = new UrlService();

	@POST
	@Path("/SHORTEN")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getShortUrl(@Context UriInfo uriInfo, @QueryParam("choiceKey") String choiceKey, @QueryParam("longUrl") String longUrl) {
		
		System.out.println(uriInfo.getBaseUri());
		// Auto-prefix if missing scheme
		String normalized = longUrl;
		if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
			normalized = "http://" + normalized;
		}

		if (choiceKey.isEmpty() || (choiceKey.length() > 5 && choiceKey.length() < 13)) {
			if (isValidWebUrl(normalized)) {
				//String shortUrl = urlService.createShortUrl(normalized, choiceKey);
				URLEntity urlEntity = urlService.createShortUrl(normalized, choiceKey);
				if (urlEntity == null)
					return Response.status(Status.BAD_REQUEST)
							       .entity("ENTERED URL IS INVALID").build(); //"ENTERED URL IS INVALID in createUrl Method";
				else
					return Response.status(Status.OK)
							        .entity(urlEntity).build();

			} else
				throw new InvalidUrlException("ENTERED URL IS INVALID"); //"ENTERED URL IS INVALID";
		} else {
			throw new InvalidShortKeyException("SHORT KEY PROVIDED MUST BE BETWEEN 6 TO 12 CHARACTER LONG"); //"Length of Choice KEY must be between 6 and 12";
		}

	}



	/*
	 * METHOD TO CHECK IF THE URL IS A VALID WEB URL
	 * 
	 */
	public static boolean isValidWebUrl(String input) {
		if (input == null || input.trim().isEmpty()) {
			return false;
		}

		try {
			URI uri = new URI(input);

			// 1. Must have a valid HTTP or HTTPS scheme
			String scheme = uri.getScheme();
			if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
				return false;
			}

			// 2. Extract and check the host
			String host = uri.getHost();
			if (host == null || host.trim().isEmpty()) {
				return false;
			}

			// 3. Reject hostnames without a dot (like "abcd") unless it's explicitly
			// "localhost"
			if (!host.contains(".") && !"localhost".equalsIgnoreCase(host)) {
				return false;
			}

			return true;

		} catch (URISyntaxException e) {
			return false;
		}
	}

}
