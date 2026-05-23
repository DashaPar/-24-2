package com.workscheduler.database;

import com.workscheduler.model.ActionLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActionLogDAO {
    
    public void logAction(int userId, String username, String action, String details) {
        String sql = "INSERT INTO action_logs (user_id, username, action, details, timestamp) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, username);
            pstmt.setString(3, action);
            pstmt.setString(4, details);
            pstmt.setLong(5, System.currentTimeMillis());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<ActionLog> getLogsByUser(int userId, int limit) {
        List<ActionLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM action_logs WHERE user_id = ? ORDER BY timestamp DESC LIMIT ?";
        try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                logs.add(new ActionLog(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("action"),
                    rs.getString("details"),
                    rs.getLong("timestamp")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
    
    public List<ActionLog> getAllLogs(int limit) {
        List<ActionLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM action_logs ORDER BY timestamp DESC LIMIT ?";
        try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                logs.add(new ActionLog(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("action"),
                    rs.getString("details"),
                    rs.getLong("timestamp")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
}