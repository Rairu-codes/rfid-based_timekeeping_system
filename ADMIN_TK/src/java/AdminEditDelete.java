    /*
     * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
     * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
     */

    import jakarta.servlet.RequestDispatcher;
    import java.io.IOException;
    import java.io.PrintWriter;
    import jakarta.servlet.ServletException;
    import jakarta.servlet.annotation.WebServlet;
    import jakarta.servlet.http.HttpServlet;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpServletResponse;
    import java.sql.Connection;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.SQLException;
    import java.util.logging.Level;
    import java.util.logging.Logger;

    /**
     *
     * @author User
     */
    import jakarta.servlet.RequestDispatcher;
    import jakarta.servlet.ServletException;
    import jakarta.servlet.annotation.WebServlet;
    import jakarta.servlet.http.HttpServlet;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpServletResponse;
    import java.io.IOException;
    import java.sql.*;
    import java.util.ArrayList;
    import java.util.List;
    import java.io.*;
    import java.sql.*;
    import java.util.*;
    import jakarta.servlet.*;
    import jakarta.servlet.annotation.WebServlet;
    import jakarta.servlet.http.*;


    /**
     * Servlet for adding and managing users
     */

@WebServlet("/AdminEditDelete")
public class AdminEditDelete extends HttpServlet {
     private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:mysql://10.44.0.56:3306/TK_DB";
    private static final String DB_USER = "Host";
    private static final String DB_PASSWORD = "Pass_123";

    // Method to check if the email already exists in the database (excluding the current user if editing)
    private boolean emailExists(Connection connection, String email, Integer userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?" +
                       (userId != null ? " AND user_id != ?" : "");
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            if (userId != null) {
                stmt.setInt(2, userId);
            }
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Returns true if email exists (for another user)
            }
        }
        return false;
    }

    // Method to check if the RFID already exists in the database (excluding the current user if editing)
    private boolean rfidExists(Connection connection, String rfid, Integer userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE rfid_tag = ?" +
                       (userId != null ? " AND user_id != ?" : "");
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, rfid);
            if (userId != null) {
                stmt.setInt(2, userId);
            }
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Returns true if RFID exists (for another user)
            }
        }
        return false;
    }

    // Method to check if an emp_id already exists in the database
    private boolean empIdExists(Connection connection, String empId) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE emp_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, empId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Returns true if emp_id already exists
            }
        }
        return false;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String action = request.getParameter("action");
            String userID = request.getParameter("id");

            // Deleting user
            if (action != null && action.equals("delete") && userID != null) {
                deleteUser(connection, Integer.parseInt(userID));
                request.setAttribute("message", "User deleted successfully!");

                // Instead of forwarding, perform a redirect to refresh the page with the updated list
                response.sendRedirect(request.getContextPath() + "/AdminEditDelete");
                return;
            }

            // Get search and filter parameters from the request
            String empIdSearch = request.getParameter("emp_id");  // Search by Employee ID
            String sortPosition = request.getParameter("sortPosition");  // Sort by Position
            String sortDepartment = request.getParameter("sortDepartment");  // Sort by Department

            // Construct SQL query with conditions based on filters
            StringBuilder queryBuilder = new StringBuilder("SELECT user_id, name, rfid_tag, email, position, department, created_at, modified_by, shift_type, emp_id FROM users WHERE 1=1");

            // Apply filter for emp_id if provided
            if (empIdSearch != null && !empIdSearch.isEmpty()) {
                queryBuilder.append(" AND emp_id LIKE ?");
            }

            // Apply filter for position if selected
            if (sortPosition != null && !sortPosition.isEmpty()) {
                queryBuilder.append(" AND position = ?");
            }

            // Apply filter for department if selected
            if (sortDepartment != null && !sortDepartment.isEmpty()) {
                queryBuilder.append(" AND department = ?");
            }

            // Finalize the query string
            String query = queryBuilder.toString();

            // Prepare the statement and set the parameters based on the filters
            pstmt = connection.prepareStatement(query);

            int paramIndex = 1;

            // Set parameter for emp_id search if provided
            if (empIdSearch != null && !empIdSearch.isEmpty()) {
                pstmt.setString(paramIndex++, "%" + empIdSearch + "%");
            }

            // Set parameter for position filter if provided
            if (sortPosition != null && !sortPosition.isEmpty()) {
                pstmt.setString(paramIndex++, sortPosition);
            }

            // Set parameter for department filter if provided
            if (sortDepartment != null && !sortDepartment.isEmpty()) {
                pstmt.setString(paramIndex++, sortDepartment);
            }

            // Execute the query
            rs = pstmt.executeQuery();

            List<Map<String, Object>> userList = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("user_id", rs.getInt("user_id"));
                userData.put("name", rs.getString("name"));
                userData.put("rfid_tag", rs.getString("rfid_tag"));
                userData.put("email", rs.getString("email"));
                userData.put("position", rs.getString("position"));
                userData.put("department", rs.getString("department"));
                userData.put("shift_type", rs.getString("shift_type"));
                userData.put("emp_id", rs.getString("emp_id"));
                userData.put("created_at", rs.getTimestamp("created_at"));
                userData.put("modified_by", rs.getString("modified_by"));
                userList.add(userData);
            }

            // Adding the user list to the request to display in the JSP
            request.setAttribute("userList", userList);

            // Forward to the userpage.jsp
            RequestDispatcher dispatcher = request.getRequestDispatcher("AdminEditDelete.jsp");
            dispatcher.forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        String name = request.getParameter("name");
        String rfid_tag = request.getParameter("rfid_tag");
        String email = request.getParameter("email");
        String position = request.getParameter("position");
        String department = request.getParameter("department");
        String modified_by = request.getParameter("modified_by");
        String shift_type = request.getParameter("shift_type");
        String userId = request.getParameter("id");
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Get the logged-in user's username from the session
            String loggedInUser = (String) request.getSession().getAttribute("username"); // Assuming you store the logged-in username in the session

            // Determine if this is an edit or add operation
            Integer userIdInt = (userId != null) ? Integer.parseInt(userId) : null;

            // Check if the email already exists before adding or editing
            if (emailExists(connection, email, userIdInt)) {
                request.setAttribute("errorMessage", "This email is already in use. Please choose a different one.");
                request.getRequestDispatcher("AdminEditDelete.jsp").forward(request, response);
                return;
            }

            // Check if the RFID already exists before adding or editing
            if (rfidExists(connection, rfid_tag, userIdInt)) {
                request.setAttribute("errorMessage", "This RFID is already in use. Please choose a different one.");
                request.getRequestDispatcher("AdminEditDelete.jsp").forward(request, response);
                return;
            }

            if ("add".equals(action)) {
                // Get the last inserted ID from the database (this assumes auto-increment ID for simplicity)
                String selectLastIdQuery = "SELECT MAX(user_id) FROM users";
                pstmt = connection.prepareStatement(selectLastIdQuery);
                rs = pstmt.executeQuery();
                int lastId = 0;
                if (rs.next()) {
                    lastId = rs.getInt(1);
                }

                // Determine ID prefix based on the position
                String idPrefix = "EMP-";  // Default prefix for employees
                if ("Intern".equalsIgnoreCase(position)) {
                    idPrefix = "OJT-";  // Special prefix for Interns
                }

                // Generate the emp_id with prefix and the next number
                String newEmpId = idPrefix + String.format("%05d", (lastId + 1));

                // Ensure that the emp_id is unique
                while (empIdExists(connection, newEmpId)) {
                    lastId++;  // Increment the ID until a unique emp_id is found
                    newEmpId = idPrefix + String.format("%05d", (lastId + 1));  // Format as 5 digits
                }

              // Insert the new user with the generated emp_id
                String insertQuery = "INSERT INTO users (name, email, position, department, emp_id, rfid_tag, modified_by, shift_type,) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                pstmt = connection.prepareStatement(insertQuery);
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, position);
                pstmt.setString(4, department);
                pstmt.setString(5, newEmpId);  // Insert the emp_id (with prefix)
                pstmt.setString(6, rfid_tag);
                pstmt.setString(7, loggedInUser); // Store the logged-in user who made the modification
                pstmt.setString(8, shift_type);
                pstmt.executeUpdate();
                request.setAttribute("message", "User added successfully!");
            } else if ("edit".equals(action) && userId != null) {
               String updateQuery = "UPDATE users SET name = ?, email = ?, position = ?, department = ?, rfid_tag = ?, modified_by = ?, shift_type = ?  WHERE user_id = ?";
                pstmt = connection.prepareStatement(updateQuery);
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, position);
                pstmt.setString(4, department);
                pstmt.setString(5, rfid_tag);
                pstmt.setString(6, loggedInUser); // Store the logged-in user who made the modification
                pstmt.setString(7, shift_type);
                pstmt.setInt(8, Integer.parseInt(userId));
                pstmt.executeUpdate();
                request.setAttribute("message", "User updated successfully!");
            }

            // After adding or editing, redirect to refresh the user list
            response.sendRedirect(request.getContextPath() + "/AdminEditDelete");

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteUser(Connection connection, int userID) throws SQLException {
        String deleteQuery = "DELETE FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteQuery)) {
            pstmt.setInt(1, userID);
            pstmt.executeUpdate();
        }
    }
}
