/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import java.text.*;

public class AdminViewLogs extends HttpServlet {
      private static final String DB_URL = "jdbc:mysql://10.44.0.56:3306/TK_DB";
    private static final String DB_USER = "Host";
    private static final String DB_PASSWORD = "Pass_123"; // Replace with your MySQL password

    // JDBC Connection
    private Connection connectToDatabase() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // MySQL JDBC Driver
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            throw new SQLException("Database connection failed.", e);
        }
    }

    // Fetch logs and user details from the database with search and sort functionality
    private List<Map<String, Object>> fetchLogs(String empIdSearch, String sortPosition, String sortDepartment) throws SQLException {
        List<Map<String, Object>> logsList = new ArrayList<>();
        Map<String, Map<String, Object>> employeeStats = new HashMap<>();

         StringBuilder queryBuilder = new StringBuilder("SELECT l.log_id, l.emp_id, l.rfid_tag, u.name, u.position, u.department, u.shift_type, l.time_in, l.time_out, l.time_in_status, l.time_out_status, l.shift_type, l.holiday, l.sunday " +
                                                       "FROM logs l " +
                                                       "LEFT JOIN users u ON l.emp_id = u.emp_id WHERE 1=1");

        // Apply search filters
        if (empIdSearch != null && !empIdSearch.isEmpty()) {
            queryBuilder.append(" AND l.emp_id LIKE ?");
        }
        if (sortPosition != null && !sortPosition.isEmpty()) {
            queryBuilder.append(" AND u.position = ?");
        }
        if (sortDepartment != null && !sortDepartment.isEmpty()) {
            queryBuilder.append(" AND u.department = ?");
        }

        // Apply sorting by position and department
        if (sortPosition != null && !sortPosition.isEmpty()) {
            queryBuilder.append(" ORDER BY u.position");
        }
        if (sortDepartment != null && !sortDepartment.isEmpty()) {
            queryBuilder.append(", u.department");
        }

        try (Connection conn = connectToDatabase(); PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString())) {
            int paramIndex = 1;

            // Set parameters for search filters
            if (empIdSearch != null && !empIdSearch.isEmpty()) {
                stmt.setString(paramIndex++, "%" + empIdSearch + "%");
            }
            if (sortPosition != null && !sortPosition.isEmpty()) {
                stmt.setString(paramIndex++, sortPosition);
            }
            if (sortDepartment != null && !sortDepartment.isEmpty()) {
                stmt.setString(paramIndex++, sortDepartment);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String logId = rs.getString("log_id");
                    String empId = rs.getString("emp_id");
                    String rfid = rs.getString("rfid_tag");
                    String name = rs.getString("name");
                    String position = rs.getString("position");
                    String department = rs.getString("department");
                    String shift_type = rs.getString("shift_type");
                    String holiday = rs.getString("holiday");
                    String sunday = rs.getString("sunday");
                    Timestamp timeIn = rs.getTimestamp("time_in");
                    Timestamp timeOut = rs.getTimestamp("time_out");
                    String time_in_status = rs.getString("time_in_status");
                    String time_out_status = rs.getString("time_out_status");
                    // Handle potential null values for time_in and time_out
                    if (timeIn == null || timeOut == null) {
                        continue;  // Skip records with missing times
                    }

                    // Calculate the worked hours
                    long workedSeconds = (timeOut.getTime() - timeIn.getTime()) / 1000;  // in seconds
                    double workedHours = workedSeconds / 3600.0;  // convert to hours

                    // Create the log entry
                    Map<String, Object> log = new HashMap<>();
                    log.put("log_id", logId);
                    log.put("emp_id", empId);
                    log.put("rfid_tag", rfid);
                    log.put("name", name);
                    log.put("position", position);
                    log.put("department", department);
                    log.put("shift_type", shift_type);
                    log.put("time_in", timeIn);
                    log.put("time_out", timeOut);
                    log.put("time_in_status", time_in_status);
                    log.put("time_out_status", time_out_status);
                    log.put("holiday", holiday);
                    log.put("sunday", sunday);
                    log.put("worked_hours", workedHours);

                    logsList.add(log);

                    // Update the employee stats (total hours, days worked)
                    if (!employeeStats.containsKey(empId)) {
                        Map<String, Object> stats = new HashMap<>();
                        stats.put("total_hours", 0.0);
                        stats.put("days_worked", new HashSet<String>());
                        employeeStats.put(empId, stats);
                    }

                    Map<String, Object> stats = employeeStats.get(empId);
                    stats.put("total_hours", (Double) stats.get("total_hours") + workedHours);

                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
                    String workDate = dateFormatter.format(timeIn);
                    ((HashSet<String>) stats.get("days_worked")).add(workDate);
                }

                // Now calculate average hours for each employee
                for (Map<String, Object> log : logsList) {
                    String empId = (String) log.get("emp_id");
                    Map<String, Object> stats = employeeStats.get(empId);

                    int daysWorked = ((HashSet<String>) stats.get("days_worked")).size();
                    log.put("days_worked", daysWorked);
                    log.put("avg_hours", daysWorked > 0 ? (Double) stats.get("total_hours") / daysWorked : 0.0);
                }
            }
        }

        return logsList;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String empIdSearch = request.getParameter("emp_id");  // Search by Employee ID
        String sortPosition = request.getParameter("sortPosition");  // Sort by Position
        String sortDepartment = request.getParameter("sortDepartment");  // Sort by Department

        // Check for null parameters and set defaults where necessary
        empIdSearch = empIdSearch == null ? "" : empIdSearch;
        sortPosition = sortPosition == null ? "" : sortPosition;
        sortDepartment = sortDepartment == null ? "" : sortDepartment;

        try {
            // Fetch the logs from the database with the search and sort filters applied
            List<Map<String, Object>> logsList = fetchLogs(empIdSearch, sortPosition, sortDepartment);

            // Set logs as a request attribute
            request.setAttribute("logsList", logsList);

            // Forward the request to logs.jsp
            RequestDispatcher dispatcher = request.getRequestDispatcher("AdminViewLogs.jsp");
            dispatcher.forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching logs from the database.");
        }
    }
}
