package com.workscheduler.database;

import com.workscheduler.model.Task;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    public boolean addTask(Task task) {
        String sql = "INSERT INTO tasks (user_id, title, description, category, start_time, end_time, " +
                "estimated_minutes, actual_minutes, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, task.getUserId());
            pstmt.setString(2, task.getTitle());
            pstmt.setString(3, task.getDescription());
            pstmt.setString(4, task.getCategory());
            pstmt.setString(5, task.getStartTime() != null ? task.getStartTime().toString() : null);
            pstmt.setString(6, task.getEndTime() != null ? task.getEndTime().toString() : null);
            pstmt.setInt(7, task.getEstimatedMinutes());
            pstmt.setInt(8, task.getActualMinutes());
            pstmt.setString(9, task.getStatus());
            pstmt.setLong(10, task.getCreatedAt());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Task> getTasksByUser(int userId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE user_id = ? ORDER BY start_time DESC";
        try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                tasks.add(extractTask(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    public boolean updateTask(Task task) {
        String sql = "UPDATE tasks SET title = ?, description = ?, category = ?, start_time = ?, " +
                "end_time = ?, estimated_minutes = ?, actual_minutes = ?, status = ? " +
                "WHERE id = ? AND user_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setString(3, task.getCategory());
            pstmt.setString(4, task.getStartTime() != null ? task.getStartTime().toString() : null);
            pstmt.setString(5, task.getEndTime() != null ? task.getEndTime().toString() : null);
            pstmt.setInt(6, task.getEstimatedMinutes());
            pstmt.setInt(7, task.getActualMinutes());
            pstmt.setString(8, task.getStatus());
            pstmt.setInt(9, task.getId());
            pstmt.setInt(10, task.getUserId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTask(int taskId, int userId) {
        String sql = "DELETE FROM tasks WHERE id = ? AND user_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Task getTaskById(int taskId, int userId) {
        String sql = "SELECT * FROM tasks WHERE id = ? AND user_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractTask(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Task extractTask(ResultSet rs) throws SQLException {
        String startTimeStr = rs.getString("start_time");
        String endTimeStr = rs.getString("end_time");
        return new Task(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("category"),
                startTimeStr != null ? LocalDateTime.parse(startTimeStr) : null,
                endTimeStr != null ? LocalDateTime.parse(endTimeStr) : null,
                rs.getInt("estimated_minutes"),
                rs.getInt("actual_minutes"),
                rs.getString("status"),
                rs.getLong("created_at")
        );
    }
}