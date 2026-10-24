package com.hcl.portal.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcl.portal.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Database Persistence & JDBC Validation Utility
 * Supports enterprise JDBC connections (MySQL 8.x) as well as
 * local persistent data validation for hermetic student capstone execution.
 */
public class DatabaseUtil {
    private static final Logger logger = LogManager.getLogger(DatabaseUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Connection getMySqlConnection() {
        ConfigManager cfg = ConfigManager.getInstance();
        String url = cfg.getProperty("db.mysql.url", "jdbc:mysql://localhost:3306/employee_portal");
        String user = cfg.getProperty("db.mysql.username", "root");
        String pass = cfg.getProperty("db.mysql.password", "");

        try {
            logger.info("Attempting JDBC connection to: {}", url);
            return DriverManager.getConnection(url, user, pass);
        } catch (Exception e) {
            logger.warn("MySQL service not reachable at {}. Details: {}", url, e.getMessage());
            return null;
        }
    }

    /**
     * Executes parameterized query to retrieve employee record
     */
    public static Map<String, Object> getEmployeeById(String employeeId) {
        Connection conn = getMySqlConnection();
        if (conn != null) {
            String sql = "SELECT * FROM employees WHERE employee_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, employeeId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("employeeId", rs.getString("employee_id"));
                        map.put("name", rs.getString("full_name"));
                        map.put("email", rs.getString("email"));
                        map.put("department", rs.getString("department"));
                        map.put("designation", rs.getString("designation"));
                        map.put("leaveBalance", rs.getInt("leave_balance"));
                        return map;
                    }
                }
            } catch (Exception e) {
                logger.error("Error executing JDBC prepared statement: {}", e.getMessage(), e);
            }
        }

        // Hermetic fallback: Read from persistent data store
        return readEmployeeFromStore(employeeId);
    }

    /**
     * Executes parameterized query to retrieve leave record
     */
    public static Map<String, Object> getLeaveById(int leaveId) {
        Connection conn = getMySqlConnection();
        if (conn != null) {
            String sql = "SELECT * FROM leaves WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, leaveId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", rs.getInt("id"));
                        map.put("employeeId", rs.getString("employee_id"));
                        map.put("leaveType", rs.getString("leave_type"));
                        map.put("startDate", rs.getString("start_date"));
                        map.put("endDate", rs.getString("end_date"));
                        map.put("days", rs.getInt("days"));
                        map.put("status", rs.getString("status"));
                        return map;
                    }
                }
            } catch (Exception e) {
                logger.error("Error executing JDBC prepared statement: {}", e.getMessage(), e);
            }
        }

        // Hermetic fallback: Read from persistent data store
        return readLeaveFromStore(leaveId);
    }

    private static Map<String, Object> readEmployeeFromStore(String employeeId) {
        try {
            File file = new File(ConfigManager.getInstance().getDatabasePath());
            if (!file.exists()) return null;

            JsonNode root = objectMapper.readTree(file);
            JsonNode employees = root.get("employees");
            if (employees != null && employees.isArray()) {
                for (JsonNode emp : employees) {
                    if (employeeId.equalsIgnoreCase(emp.get("employeeId").asText())) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("employeeId", emp.get("employeeId").asText());
                        map.put("name", emp.get("name").asText());
                        map.put("email", emp.get("email").asText());
                        map.put("department", emp.get("department").asText());
                        map.put("designation", emp.get("designation").asText());
                        map.put("leaveBalance", emp.get("leaveBalance").asInt());
                        return map;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to read employee persistence store: {}", e.getMessage());
        }
        return null;
    }

    private static Map<String, Object> readLeaveFromStore(int leaveId) {
        try {
            File file = new File(ConfigManager.getInstance().getDatabasePath());
            if (!file.exists()) return null;

            JsonNode root = objectMapper.readTree(file);
            JsonNode leaves = root.get("leaves");
            if (leaves != null && leaves.isArray()) {
                for (JsonNode l : leaves) {
                    if (leaveId == l.get("id").asInt()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", l.get("id").asInt());
                        map.put("employeeId", l.get("employeeId").asText());
                        map.put("leaveType", l.get("leaveType").asText());
                        map.put("startDate", l.get("startDate").asText());
                        map.put("endDate", l.get("endDate").asText());
                        map.put("days", l.get("days").asInt());
                        map.put("status", l.get("status").asText());
                        return map;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to read leave persistence store: {}", e.getMessage());
        }
        return null;
    }
}
