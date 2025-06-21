    import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.logging.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.URLEncoder;

public class CreateAdminServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

     private static final String DB_URL = "jdbc:mysql://10.44.0.56:3306/TK_DB";
    private static final String DB_USER = "Host";
    private static final String DB_PASSWORD = "Pass_123";

    // Method to check if the username already exists
    private boolean usernameExists(Connection connection, String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM admin WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Returns true if username exists
            }
        }
        return false;
    }

    // Method to check if the role_id exists in the roles table
    private boolean roleExists(Connection connection, int roleId) throws SQLException {
        String query = "SELECT COUNT(*) FROM roles WHERE role_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, roleId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Returns true if role_id exists
            }
        }
        return false;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        int roleId = Integer.parseInt(request.getParameter("roleId")); // Assuming you're passing roleId as a parameter

        // Check if username already exists
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Check if the username exists
            if (usernameExists(connection, username)) {
                response.sendRedirect("CreateAdminAccount.jsp?error=" + URLEncoder.encode("This username is already in use. Please choose a different one.", "UTF-8"));
                return;
            }

            // Check if the role_id exists in the roles table
            if (!roleExists(connection, roleId)) {
                response.sendRedirect("CreateAdminAccount.jsp?error=" + URLEncoder.encode("Invalid role. Please select a valid role.", "UTF-8"));
                return;
            }

            // Hash the password (use SHA-256 or any secure method)
            String hashedPassword = hashPassword(password);  // Ensure we're using SHA-256 hashing here

            // Insert new admin into the database
            String insertQuery = "INSERT INTO admin (username, password, role_id) VALUES (?, ?, ?)";
            pstmt = connection.prepareStatement(insertQuery);
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setInt(3, roleId);

            int rowsAffected = pstmt.executeUpdate();

            // Check if the insertion was successful
            if (rowsAffected > 0) {
                response.sendRedirect("CreateAdminAccount.jsp?success=" + URLEncoder.encode("Admin account created successfully!", "UTF-8"));
            } else {
                response.sendRedirect("CreateAdminAccount.jsp?error=" + URLEncoder.encode("Failed to create admin account.", "UTF-8"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("CreateAdminAccount.jsp?error=" + URLEncoder.encode("Database error: " + e.getMessage(), "UTF-8"));
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

    // A simple password hashing function using SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b)); // Convert to hex
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
