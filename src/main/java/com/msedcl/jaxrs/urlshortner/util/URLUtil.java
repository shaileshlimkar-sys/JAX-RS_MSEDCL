package com.msedcl.jaxrs.urlshortner.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

public class URLUtil {
	
	/*
	 * Method to replace passed parameters in the long url parameters.
	 * 
	 */
	public static String getReplacedParametersString(String passedParameters, String originalParameters) {
		String[] contextArray = passedParameters.split("&");
		String[] URLArray = originalParameters.split("&");
		Map<String, String> finalMap = new HashMap<String, String>();

		for (String urlArg : URLArray) {
			String[] urlArgValuePair = urlArg.split("=");
			finalMap.put(urlArgValuePair[0], urlArgValuePair[1]);
		}

		for (String contextArg : contextArray) {
			String[] contextArgValue = contextArg.split("=");
			finalMap.put(contextArgValue[0], contextArgValue[1]);

		}
		StringBuilder URLtoReturn = new StringBuilder("");
		// finalMap.forEach((key , value) -> System.out.println("KEY is : " + key + "
		// Value is : "+value));
		finalMap.forEach((key, value) -> {
			if (URLtoReturn.length() > 1)
				URLtoReturn.append("&").append(key).append("=").append(value);
			else
				URLtoReturn.append(key).append("=").append(value);
		});

		return URLtoReturn.toString();
	}

	/*
	 * METHOD TO CREATE PARAMETER STRING FROM THE SHORT url
	 */

	public static String getParameterText(MultivaluedMap<String, String> queryParams) {
		// MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		StringBuilder sb = new StringBuilder("");
		if (!queryParams.isEmpty()) {
			queryParams.forEach((key, values) -> {
				System.out.println("Key: " + key);
				// Loop through the list of values for this specific key
				for (String value : values) {
					if (sb.length() > 0) ////////// if there are multiple params add & between each pair
						sb.append('&');

					System.out.println("  Value: " + value);
					sb.append(key + "=" + value);
				}
			});
		}

		return sb.toString();
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
	
	/*
	 * METHOD TO CHECK IF THE URL ACTUALLY EXISTS
	 */
    public static boolean doesUrlExist(String urlString) {
        try {
            // Using URI to convert to URL avoids deprecated URL constructors in newer JDKs
            URL url = new URI(urlString).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // Use HEAD to only fetch headers (saves time and bandwidth)
            connection.setRequestMethod("HEAD");
            
            // Set reasonable timeouts so your code doesn't hang indefinitely
            connection.setConnectTimeout(3000); // 3 seconds
            connection.setReadTimeout(3000);    // 3 seconds
            
            // Get the HTTP status code
            int responseCode = connection.getResponseCode();
            
            // A 200 OK means the URL exists and is active
            return (responseCode == HttpURLConnection.HTTP_OK);
            
        } catch (URISyntaxException | IOException e) {
            // Malformed URL, connection failure, or timeout
            return false;
        }
    }

}
