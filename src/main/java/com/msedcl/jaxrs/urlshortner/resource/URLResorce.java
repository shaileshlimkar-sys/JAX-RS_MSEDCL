package com.msedcl.jaxrs.urlshortner.resource;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.msedcl.jaxrs.urlshortner.exception.InvalidShortKeyException;
import com.msedcl.jaxrs.urlshortner.exception.InvalidUrlException;
import com.msedcl.jaxrs.urlshortner.model.ErrorMessage;
import com.msedcl.jaxrs.urlshortner.model.URLEntity;
import com.msedcl.jaxrs.urlshortner.service.UrlService;

@Path("/")
public class URLResorce {

	UrlService urlService = new UrlService();

	@POST
	@Path("/SHORTEN")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getShortUrl(@Context UriInfo uriInfo, @QueryParam("choiceKey") String choiceKey,
			@QueryParam("longUrl") String longUrl) {

		if (choiceKey == null || longUrl == null)
			return Response.status(Status.BAD_REQUEST)
					.entity(new ErrorMessage("BOTH choiceKey AND longUrl ARGUMENS ARE MANDATORY", 400)).build();

		// Auto-prefix if missing scheme
		String normalized = longUrl;
		if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
			normalized = "http://" + normalized;
		}

		if (choiceKey.isEmpty() || (choiceKey.length() > 5 && choiceKey.length() < 13)) {
			if (isValidWebUrl(normalized)) {
				// String shortUrl = urlService.createShortUrl(normalized, choiceKey);
				URLEntity urlEntity = urlService.createShortUrl(normalized, choiceKey);
				if (urlEntity == null)
					return Response.status(Status.BAD_REQUEST).entity("ENTERED URL IS INVALID").build();
				else {
					String createdKey = urlEntity.getShortUrl();
					urlEntity.setShortUrl(uriInfo.getBaseUriBuilder().path(createdKey).toString());
					return Response.status(Status.OK).entity(urlEntity).build();
				}

			} else
				throw new InvalidUrlException("ENTERED URL IS INVALID"); // "ENTERED URL IS INVALID";
		} else {
			throw new InvalidShortKeyException("SHORT KEY PROVIDED MUST BE BETWEEN 6 TO 12 CHARACTER LONG");
		}

	}

	@GET
	@Path("/{shortUrl}")
	public Response redirect(@PathParam("shortUrl") String shotrUrl) {
		if (shotrUrl == null)
			return Response.status(Status.BAD_REQUEST)
					.entity(new ErrorMessage("shotrUrl ARGUMENT IS MISSING IN THE REQUEST", 400)).build();

		System.out.println("GET REQUESTED " + shotrUrl);
		String longUrl = urlService.getLongUrl(shotrUrl);

		URI targetUri = URI.create(longUrl);
		return Response.status(Status.FOUND).location(targetUri).build();  //Redirection

	}

	/*
	 * METHOD TO GET INIFORMATION ABOUT A SHORT KEY Path Parameter is the SHORT KEY
	 * for which QR code is to be generated
	 */
	@GET
	@Path("/Url/{shortUrl}")
	public Response getUrlInfo(@Context UriInfo uriInfo, @PathParam("shortUrl") String shotrUrl) {
		if (shotrUrl == null)
			return Response.status(Status.BAD_REQUEST)
					.entity(new ErrorMessage("shotrUrl ARGUMENT IS MISSING IN THE REQUEST", 400)).build();

		String longUrl = urlService.getLongUrl(shotrUrl);
		URLEntity urlEntity = new URLEntity(longUrl, uriInfo.getBaseUriBuilder().path(shotrUrl).toString());

		return Response.status(Status.FOUND).entity(urlEntity).build();

	}

	/*
	 * METHOD TO GENERATE QR CODE Path Parameter is the SHORT KEY for which QR code
	 * is to be generated
	 */
	@POST
	@Path("/generateQR/{shortUrl}")
	public Response generateQR(@PathParam("shortUrl") String shotrUrl) {

		byte[] QRCodeBytes = urlService.generateQR(shotrUrl);
		if (QRCodeBytes != null)
			return Response.status(Status.CREATED).entity(QRCodeBytes).build();
		else
			return Response.status(Status.BAD_REQUEST).entity("BAD REQUEST ").build();
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
