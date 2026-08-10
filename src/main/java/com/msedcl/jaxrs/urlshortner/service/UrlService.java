package com.msedcl.jaxrs.urlshortner.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.msedcl.jaxrs.urlshortner.db.PlainDatabaseConfig;
import com.msedcl.jaxrs.urlshortner.exception.GenericException;
import com.msedcl.jaxrs.urlshortner.exception.InvalidShortKeyException;
import com.msedcl.jaxrs.urlshortner.model.URLEntity;
import com.msedcl.jaxrs.urlshortner.util.KeyGenerator;
import com.msedcl.jaxrs.urlshortner.util.QRUtil;

import io.nayuki.qrcodegen.QrCode;

public class UrlService {

	private final String CHECK_HOST_SQL = "select * from host WHERE host_url = ?";
	private String CHECK_PATH_SQL = "select * from path WHERE path_url = ?";
	private String CHECK_PATH_AND_KEY_SQL = "select * from path WHERE path_url = ? AND path_key = ?";
	private String CHECK_QR_SQL = "select * from qr_codes WHERE path_url= ?";

	private final String INSERT_HOST_SQL = "insert into host (host_url, host_key) values (?, ?)";
	private final String INSERT_PATH_SQL = "insert into path (path_url, path_key, hostId) values (?, ?, ?)";
	private final String INSERT_QR_SQL = "insert into qr_codes (path_url , image_data) values(?, ?)";

	private final String GET_LONG_URL = "select path_url from path where path_key = ?";
	
    // HK2 automatically injects the Hikari DataSource here
//    @Inject
//    private DataSource dataSource;

	public UrlService() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * Method to insert Short Key
	 */
	public URLEntity createShortUrl(String longUrl, String choiceKey) {
		System.out.println("CREATE URL METHOD CALLED");
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

			return null; // "WRONG URL";
		}
		System.out.println("OPENING CONNECTION TO THE DATABASE");
		// This opens a physical network link, executes, and auto-closes it
		// "try-with-resources" BLOCK
		try (Connection conn = PlainDatabaseConfig.createNewConnection();
				PreparedStatement checkHostStmt = conn.prepareStatement(CHECK_HOST_SQL);
				PreparedStatement insertHostStmt = conn.prepareStatement(INSERT_HOST_SQL,
						Statement.RETURN_GENERATED_KEYS)) {
			System.out.println("CONNECTION TO THE DATABASE ESTABLISHED");
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

					createdHostKey = rs.getString("host_key"); // "HOST NAME IS ALREADY REGISTERED THE HOST KEY is to be
																// returned
					System.out.println("************* HOST ALREADY PRESENT " + createdHostKey);

					hostId = rs.getLong("hostID");
					System.out.println("************* HOST ID is ALREADY PRESENT " + hostId);
				}

			}
		} catch (SQLException e) {
			//e.printStackTrace();
			throw new RuntimeException("SQL execution failed: " + e.getMessage(), e);

		}
		///////////////////// HOST URL PART IS OVER
		///////////////////// NOW THE REST OF THE PATH

		// This opens a physical network link, executes, and auto-closes it
		// "try-with-resources" BLOCK
		String CONDITIONAL_PATH_CHECK;
		if (choiceKey.isEmpty()) // if no choice key is not given check only path
			CONDITIONAL_PATH_CHECK = CHECK_PATH_SQL; // if choice key is given, check for path and key combination
		else
			CONDITIONAL_PATH_CHECK = CHECK_PATH_AND_KEY_SQL;

		System.out.println("************* HOST ID is " + hostId);
		try (Connection conn = PlainDatabaseConfig.createNewConnection();
				PreparedStatement checkPathStmt = conn.prepareStatement(CONDITIONAL_PATH_CHECK);
				PreparedStatement insertPathStmt = conn.prepareStatement(INSERT_PATH_SQL)) {

			checkPathStmt.setString(1, longUrl);
			if (!choiceKey.isEmpty())
				checkPathStmt.setString(2, choiceKey);

			try (ResultSet rs = checkPathStmt.executeQuery()) {

				if (!rs.next()) { /// check if the host is not present in master

					insertPathStmt.setString(1, longUrl);
					if (choiceKey.isEmpty()) { /// if the choice for the short URL is not given
						String pathKey = KeyGenerator.generateKey(5); /// generate a random key
						createdPAthKey = pathKey; /// final host short URL
					} else
						createdPAthKey = choiceKey; /// if the choice for the short URL is given then use the choice

					insertPathStmt.setString(2, createdPAthKey);
					insertPathStmt.setLong(3, hostId);
					
					try {
					insertPathStmt.executeUpdate();
					System.out.println("************* PATH is NEWLY CREATED " + createdPAthKey);
					}
					catch(Exception e) {
						throw new InvalidShortKeyException("SHORT KEY ISSUE : " + e.getMessage());
					}
					

				} else {
					createdPAthKey = rs.getString("path_key"); // "PATH IS ALREADY REGISTERED THE PATH is to be
																// returned
					System.out.println("************* PATH is ALREADY PRESENT " + createdPAthKey);
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new GenericException("INTERNAL SERVER ERROR : "+ e.getMessage());
		}

		URLEntity urlEntity = new URLEntity(longUrl, createdPAthKey);
		// return createdHostKey + "/" + createdPAthKey;
		return urlEntity;

	}

	/*
	 * METHOD to get LongURL for the short Key
	 */
	public String getLongUrl(String shortUrl) {
		String longUrl;
		try (Connection conn = PlainDatabaseConfig.createNewConnection();
				PreparedStatement getPathStmt = conn.prepareStatement(GET_LONG_URL)) {
			getPathStmt.setString(1, shortUrl);
			try {
				ResultSet rs = getPathStmt.executeQuery();
				if (rs.next()) {
					longUrl = rs.getString("path_url");
					System.out.println(longUrl);

				} else {
					//longUrl = "ENTERED SHORT URL IS NOT FOUND";
					//System.out.println(longUrl);
					throw new InvalidShortKeyException("ENTERED SHORT URL IS NOT FOUND : "+ shortUrl);
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw new GenericException("INTERNAL SERVER ERROR : "+ e.getMessage());
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new GenericException("INTERNAL SERVER ERROR : "+ e.getMessage());

		}
		return longUrl;
	}
	
	public byte[] generateQR(String shortUrl ) {
		
		String longUrl = null;
		try (Connection conn = PlainDatabaseConfig.createNewConnection();
				PreparedStatement getPathStmt = conn.prepareStatement(GET_LONG_URL)) { //////////// check if short key is present in master
			getPathStmt.setString(1, shortUrl);
			try {
				ResultSet rs = getPathStmt.executeQuery();
				if (rs.next()) {
					longUrl = rs.getString("path_url");
					System.out.println(longUrl);

				} else {
					throw new InvalidShortKeyException("ENTERED SHORT KEY IS NOT FOUND");
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw new GenericException("INTERNAL SERVER ERROR : "+ e.getMessage());
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new GenericException("INTERNAL SERVER ERROR : "+ e.getMessage());

		}
		
		byte [] qrBytes = null;
		try (Connection conn = PlainDatabaseConfig.createNewConnection();
				PreparedStatement getPathStmt = conn.prepareStatement(CHECK_QR_SQL); /////////////// check if QR code is already created
				PreparedStatement insertQrStmt = conn.prepareStatement(INSERT_QR_SQL)) {  ///////////insert into Qr codes
			getPathStmt.setString(1, longUrl);
			try {
				ResultSet rs = getPathStmt.executeQuery();
				if (rs.next()) {
					longUrl = rs.getString("path_url");
					qrBytes = rs.getBytes("image_data");
					

				} else {
					String url = longUrl;
			        QrCode qr = QrCode.encodeText(url, QrCode.Ecc.MEDIUM);
			        BufferedImage img = QRUtil.toImage(qr, 4, 4); // (Using the toImage method from QRUtil class)
			        qrBytes = QRUtil.convertImageToBytes(img);
			        
			        insertQrStmt.setString(1, longUrl);
			        insertQrStmt.setBytes(2, qrBytes);
			        
			        insertQrStmt.executeUpdate();	        
			        
				}
			} catch (SQLException  | IOException e) {
				e.printStackTrace();
				throw new GenericException("INTERNAL SERVER ERROR : "+ e.getMessage());
			}
			
		}catch (SQLException e) {
			e.printStackTrace();
			throw new GenericException("INTERNAL SERVER ERROR : "+ e.getMessage());

		}
		
		return qrBytes;		
	}
}

