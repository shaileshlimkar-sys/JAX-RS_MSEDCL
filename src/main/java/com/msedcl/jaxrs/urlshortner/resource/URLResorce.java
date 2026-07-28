package com.msedcl.jaxrs.urlshortner.resource;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import com.msedcl.jaxrs.urlshortner.dao.MemberDAO;

@Path("/")
public class URLResorce {

	@POST
	@Path("/SHORTEN")
	public String getShortUrl(@QueryParam("oldUrl") String oldUrl) {
		if(isValidWebUrl(oldUrl)) {
			try {
				URI originalURL = new URI(oldUrl);
				System.out.println("URL CREATED");
				return originalURL.toString();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				return "WRONG URL";
			} catch (Exception e) {
				return e.getMessage();
			}
		}
		else return "ENTERED URL IS INVALID";

	}
	
	@GET
	@Path("/getmember/{memberId}")
	public String getMember(@PathParam("memberId") int id) {
		MemberDAO member= new MemberDAO();
		String memberName= member.getUserEmail(id);
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

		// Auto-prefix if missing scheme
		String normalized = input;
		if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
			normalized = "https://" + normalized;
		}

		try {
			URI uri = new URI(normalized);

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
