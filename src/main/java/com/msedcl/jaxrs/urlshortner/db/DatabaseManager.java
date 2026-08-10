package com.msedcl.jaxrs.urlshortner.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

/*
 * The class to handle the lifecycle of your Hikari connection pool
 */

public class DatabaseManager {
    private static HikariDataSource dataSource;

    public static synchronized DataSource getDataSource() {
        if (dataSource == null) {
        	

            HikariConfig config = new HikariConfig();
            
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");  // Force to load MYSQL driver
            config.setJdbcUrl("jdbc:mysql://localhost:3306/url_shortner");
            
            config.setUsername("root");
            config.setPassword("root123");
            
                        
            // Recommended optimization properties
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.setMaximumPoolSize(10);

            dataSource = new HikariDataSource(config);
        
        }
        return dataSource;
    }

    public static synchronized void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}