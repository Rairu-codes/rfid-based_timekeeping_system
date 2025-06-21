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

public class AdminViewErrorLogs extends HttpServlet {
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

    // Fetch logs from the database with search and sort functionality
    private List<Map<String, Object>> fetchLogs(String empIdSearch, String sortPosition, String sortDepartment) throws SQLException {
        List<Map<String, Object>> logsList = new ArrayList<>();

        // Base query to fetch error logs along with user details
        StringBuilder queryBuilder = new StringBuilder("SELECT e.error_log_id, e.emp_id, e.rfid_tag, u.name, u.position, u.department, e.error_message, e.error_time " +
                                                       "FROM error_logs e " +
                                                       "LEFT JOIN users u ON e.emp_id = u.emp_id WHERE 1=1");

        // Apply search filters if they exist
        if (empIdSearch != null && !empIdSearch.isEmpty()) {
            queryBuilder.append(" AND e.emp_id LIKE ?");
        }
        if (sortPosition != null && !sortPosition.isEmpty()) {
            queryBuilder.append(" AND u.position = ?");
        }
        if (sortDepartment != null && !sortDepartment.isEmpty()) {
            queryBuilder.append(" AND u.department = ?");
        }

        // Apply sorting if required
        if (!sortPosition.isEmpty()) {
            queryBuilder.append(" ORDER BY u.position");
        }
        if (!sortDepartment.isEmpty()) {
            queryBuilder.append(", u.department");
        }

        // Execute the query
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
                    String errorlogId = rs.getString("error_log_id");
                    String empId = rs.getString("emp_id");
                    String rfid = rs.getString("rfid_tag");
                    String name = rs.getString("name");
                    String position = rs.getString("position");
                    String department = rs.getString("department");
                    String errorMessage = rs.getString("error_message");
                    Timestamp errorTime = rs.getTimestamp("error_time");

                    // Create a log entry
                    Map<String, Object> log = new HashMap<>();
                    log.put("error_log_id", errorlogId);
                    log.put("emp_id", empId);
                    log.put("rfid_tag", rfid);
                    log.put("name", name);
                    log.put("position", position);
                    log.put("department", department);
                    log.put("error_message", errorMessage);
                    log.put("error_time", errorTime);

                    logsList.add(log);
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
            RequestDispatcher dispatcher = request.getRequestDispatcher("AdminViewErrorLogs.jsp");
            dispatcher.forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching logs from the database.");
        }
    }
}
