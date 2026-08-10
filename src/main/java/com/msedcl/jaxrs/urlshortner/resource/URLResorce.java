package com.msedcl.jaxrs.urlshortner.resource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.msedcl.jaxrs.urlshortner.exception.InvalidShortKeyException;
import com.msedcl.jaxrs.urlshortner.exception.InvalidUrlException;
import com.msedcl.jaxrs.urlshortner.model.ErrorMessage;
import com.msedcl.jaxrs.urlshortner.model.URLEntity;
import com.msedcl.jaxrs.urlshortner.service.UrlService;
import com.msedcl.jaxrs.urlshortner.util.URLUtil;

@Path("/")
public class URLResorce {

	UrlService urlService = new UrlService();

	/*
	 * METHOD TO CREATE A SHORT URL FOR THE LONG URL SPECIFIED ALSO ACCEPTS A CHOICE
	 * FOR THE SHORT KEY.
	 */
	@POST
	@Path("/SHORTEN")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getShortUrl(@Context UriInfo uriInfo, @QueryParam("choiceKey") String choiceKey,
			@QueryParam("longUrl") String longUrl) {

		if(!URLUtil.doesUrlExist(longUrl))
			throw new InvalidUrlException("ENTERED URL IS INVALID :: CAN NOT LOCATE THE URL"); // "ENTERED URL IS INVALID";
			
		if (choiceKey == null || longUrl == null)
			return Response.status(Status.BAD_REQUEST)
					.entity(new ErrorMessage("BOTH choiceKey AND longUrl ARGUMENS ARE MANDATORY", 400)).build();

		// Auto-prefix if missing scheme
		String normalized = longUrl;
		if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
			normalized = "http://" + normalized;
		}

		if (choiceKey.isEmpty()
				|| (choiceKey.length() > 5 && choiceKey.length() < 13) && (!isHavingSpecialCharacters(choiceKey))) {
			if (URLUtil.isValidWebUrl(normalized)) {
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
			throw new InvalidShortKeyException(
					"SHORT KEY PROVIDED MUST BE BETWEEN 6 TO 12 CHARACTER LONG AND SHOULD NOT CONTAIN SPECIAL CHARACTERS");
		}

	}

	/*
	 * METHOD TO REDIRECT TO THE LONG URL BASED ON THE SHORT URL
	 */
	@GET
	@Path("/{shortUrl}")
	public Response redirect(@Context UriInfo uriInfo, @PathParam("shortUrl") String shotrUrl) {

		if (shotrUrl == null)
			return Response.status(Status.BAD_REQUEST)
					.entity(new ErrorMessage("shotrUrl ARGUMENT IS MISSING IN THE REQUEST", 400)).build();

		String longUrl = urlService.getLongUrl(shotrUrl);
		URI targetUri = null;

		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		if (queryParams.isEmpty()) { // If no parameters are passed with Short URL, then direct redirect to Long Url.
			targetUri = URI.create(longUrl);
		} 
		
		else {
			String passedParamsString = URLUtil.getParameterText(queryParams); // Create a string of all the path parameters
																		// passed alongwith the short URL.
			URI tempUri = URI.create(longUrl);
			String originalParams = tempUri.getQuery();
			if (originalParams == null) { // If long URL has no parameters, directly add passed parameters to long URL
				
				String newUrl = longUrl + '?' + passedParamsString;
				System.out.println("TRYING REDIRECT AFTER ADDING PARAMETERS to : "+ newUrl);
				targetUri = URI.create(newUrl);
			} 
			
			else {
				String parameterReplacedString = URLUtil.getReplacedParametersString(passedParamsString, originalParams);

				try {
					targetUri = new URI(tempUri.getScheme(), 
							tempUri.getUserInfo(), 
							tempUri.getHost(),
							tempUri.getPort(), 
							tempUri.getPath(), 
							parameterReplacedString, //////////// change only the parameter Query in the original URL
							tempUri.getFragment());
				} 
				
				catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new InvalidUrlException("ERROR WHILE CREATING FINAL URL");
				}

			}

		}

		//URI targetUri = URI.create(longUrl);
		return Response.status(Status.FOUND).location(targetUri).build(); // Redirection

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
	 * MRTHOD TO CHECK IF THE URL HAS SPECIAL CHARACTERS EXCEPT A-Z a-z 0-9 and
	 * space
	 */
	public static boolean isHavingSpecialCharacters(String url) {
		if (url.isEmpty())
			return true;

		boolean test = url.matches(".*[^a-zA-Z0-9_].*");
		System.out.println("CHECKING SPECIAL CHARACTER :: in " + url + " - " + test);
		return test;
	}

}
