package com.msedcl.jaxrs.urlshortner.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PlainDatabaseConfig {

	private static final String URL = "jdbc:mysql://localhost:3306/springJDBC";
	private static final String USER = "root";
	private static final String PASSWORD = "root123";

	static {
		try {
			// Force the driver to load into the web container
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("MySQL Driver not found!", e);
		}
	}

	// Creates a brand new physical database connection every single time
	public static Connection createNewConnection() throws SQLException {
		return DriverManager.getConnection(URL, USER, PASSWORD);
	}
}
