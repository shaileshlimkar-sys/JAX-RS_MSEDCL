package com.msedcl.jaxrs.urlshortner.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.msedcl.jaxrs.urlshortner.db.PlainDatabaseConfig;
import com.msedcl.jaxrs.urlshortner.model.HomeUrl;
import com.msedcl.jaxrs.urlshortner.model.URLEntity;
import com.msedcl.jaxrs.urlshortner.util.KeyGenerator;

public class UrlService {

	private final String CHECK_HOST_SQL = "select * from host WHERE host_url = ?";
	private String CHECK_PATH_SQL = "select * from path WHERE path_url = ?";

	private final String INSERT_HOST_SQL = "insert into host (host_url, host_key) values (?, ?)";
	private final String INSERT_PATH_SQL = "insert into path (path_url, path_key, hostId) values (?, ?, ?)";

	public UrlService() {
		// TODO Auto-generated constructor stub
	}

	public URLEntity createShortUrl(String longUrl, String choiceKey) {
		String host = null;
		long hostId = 0;
		String createdHostKey = null; // to store Short URL of the host
		String createdPAthKey = null; // to store short URL for Path
		try {
			URI newURI = new URI(longUrl);
			host = newURI.getHost();

		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return null; //"WRONG URL";
		}

		// This opens a physical network link, executes, and auto-closes it
		// "try-with-resources" BLOCK
		try (Connection conn = PlainDatabaseConfig.createNewConnection();
				PreparedStatement checkHostStmt = conn.prepareStatement(CHECK_HOST_SQL);
				PreparedStatement insertHostStmt = conn.prepareStatement(INSERT_HOST_SQL,
						Statement.RETURN_GENERATED_KEYS)) {

			checkHostStmt.setString(1, host);

			try (ResultSet rs = checkHostStmt.executeQuery()) {

				if (!rs.next()) { /// check if the host is not present in master

					insertHostStmt.setString(1, host);
					String hostKey = KeyGenerator.generateKey(2); /// generate a random key
					createdHostKey = hostKey + ".msedcl.in"; /// final host short URL
					insertHostStmt.setString(2, createdHostKey);

					int affectedRows = insertHostStmt.executeUpdate();
					System.out.println("************* HOST IS NEWLY CREATED " + affectedRows);
					if (affectedRows > 0) {
						try (ResultSet generatedHostID = insertHostStmt.getGeneratedKeys()) {
							if (generatedHostID.next()) {
								hostId = generatedHostID.getLong(1);
								System.out.println("************* HOST ID is CREATED " + hostId);
							}
						} catch (SQLException e) {
							System.out.println(e.getMessage());
						}
					}

				} else {

					createdHostKey = rs.getString("host_key"); // "HOST NAME IS ALREADY REGISTERED THE HOST KEY is to be returned
					System.out.println("************* HOST ALREADY PRESENT " + createdHostKey);
					
					hostId = rs.getLong("hostID");
					System.out.println("************* HOST ID is ALREADY PRESENT " + hostId);
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();

		}
		///////////////////// HOST URL PART IS OVER
		///////////////////// NOW THE REST OF THE PATH

		// This opens a physical network link, executes, and auto-closes it
		// "try-with-resources" BLOCK

		System.out.println("************* HOST ID is " + hostId);
		try (Connection conn = PlainDatabaseConfig.createNewConnection();
				PreparedStatement checkPathStmt = conn.prepareStatement(CHECK_PATH_SQL);
				PreparedStatement insertPathStmt = conn.prepareStatement(INSERT_PATH_SQL)) {

			checkPathStmt.setString(1, longUrl);

			try (ResultSet rs = checkPathStmt.executeQuery()) {

				if (!rs.next()) { /// check if the host is not present in master

					insertPathStmt.setString(1, longUrl);
					if (choiceKey.isEmpty()) { /// if the choice for the short URL is not given
						String pathKey = KeyGenerator.generateKey(5); /// generate a random key
						createdPAthKey = pathKey; /// final host short URL
					} else
						createdPAthKey = choiceKey; ///if the choice for the short URL is given then use the choice

					insertPathStmt.setString(2, createdPAthKey);
					insertPathStmt.setLong(3, hostId);

					insertPathStmt.executeUpdate();
					System.out.println("************* PATH is NEWLY CREATED " + createdPAthKey);

				} else {
					createdPAthKey = rs.getString("path_key"); // "PATH IS ALREADY REGISTERED THE PATH is to be
																// returned
					System.out.println("************* PATH is ALREADY PRESENT " + createdPAthKey);
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();

		}
		
		URLEntity urlEntity = new URLEntity(longUrl,HomeUrl.HOME_URL+createdPAthKey);
		//return createdHostKey + "/" + createdPAthKey;
		return urlEntity;
				

	}
}
