package com.msedcl.jaxrs.urlshortner.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.msedcl.jaxrs.urlshortner.db.PlainDatabaseConfig;

public class MemberDAO {

    public String getUserEmail(int userId) {
        String sql = "select * from members WHERE memberId = ?";
        
        // This opens a physical network link, executes, and auto-closes it
        //"try-with-resources" BLOCK
        try (Connection conn = PlainDatabaseConfig.createNewConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
            	
                if (rs.next()) {
                    return rs.getString("memberName");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
        return null;
    }
}