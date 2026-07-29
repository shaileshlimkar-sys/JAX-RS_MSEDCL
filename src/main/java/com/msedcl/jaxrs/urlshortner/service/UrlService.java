package com.msedcl.jaxrs.urlshortner.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.msedcl.jaxrs.urlshortner.db.PlainDatabaseConfig;
import com.msedcl.jaxrs.urlshortner.util.KeyGenerator;

public class UrlService {
	public UrlService() {
		// TODO Auto-generated constructor stub
	}

	public String createShortUrl(String inputUrl) {
		String host = null;
		String createdHostKey = null;
		try {
			URI newURI = new URI(inputUrl);
			host = newURI.getHost();

		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return "WRONG URL";
		}

		String checkHostSql = "select * from host WHERE host_url = ?";
		String checkPathSql = "select * from path WHERE path_url = ?";

		String insertHostSql = "insert into host (host_url, host_key) values (?, ?)";
		String insertPathSql = "insert into path (path_url, path_key, hostId) values (?, ?, ?)";

		// This opens a physical network link, executes, and auto-closes it
		// "try-with-resources" BLOCK
		try (Connection conn = PlainDatabaseConfig.createNewConnection();
				PreparedStatement checkHostStmt = conn.prepareStatement(checkHostSql);
				PreparedStatement insertHostStmt = conn.prepareStatement(insertHostSql)) {

			checkHostStmt.setString(1, host);

			try (ResultSet rs = checkHostStmt.executeQuery()) {

				if (!rs.next()) { /// check if the host is not present in master

					insertHostStmt.setString(1, host);
					String hostKey = KeyGenerator.generateKey(2); /// generate a random key
					createdHostKey = hostKey + ".msedcl.in"; /// final host short URL
					insertHostStmt.setString(2, createdHostKey);

					insertHostStmt.executeUpdate();

				} else
					createdHostKey = "HOST NAME IS ALREADY REGISTERED THE HOST KEY is " + rs.getString("host_key");
			}
		} catch (SQLException e) {
			e.printStackTrace();

		}
		return createdHostKey;

	}
}
