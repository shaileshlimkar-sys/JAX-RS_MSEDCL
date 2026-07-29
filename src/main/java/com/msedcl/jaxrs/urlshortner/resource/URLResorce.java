package com.msedcl.jaxrs.urlshortner.resource;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import com.msedcl.jaxrs.urlshortner.dao.MemberDAO;
import com.msedcl.jaxrs.urlshortner.service.UrlService;

@Path("/")
public class URLResorce {

	UrlService urlService = new UrlService();

	@POST
	@Path("/SHORTEN")
	public String getShortUrl(@QueryParam("choiceKey") String choiceKey, @QueryParam("oldUrl") String oldUrl) {

		// Auto-prefix if missing scheme
		String normalized = oldUrl;
		if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
			normalized = "http://" + normalized;
		}

		if (choiceKey.isEmpty() || (choiceKey.length() > 5 && choiceKey.length() < 13)) {
			if (isValidWebUrl(normalized)) {
				String shortUrl = urlService.createShortUrl(normalized, choiceKey);

				if (shortUrl == null)
					return "ENTERED URL IS INVALID in createUrl Method";
				else
					return shortUrl;

			} else
				return "ENTERED URL IS INVALID";
		} else {
			return "Length of Choice KEY must be between 6 and 12";
		}

	}

	/*
	 * Sample method to test DB connectivity
	 */
	@GET
	@Path("/getmember/{memberId}")
	public String getMember(@PathParam("memberId") int id) {
		MemberDAO member = new MemberDAO();
		String memberName = member.getUserEmail(id);
		return memberName;
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
