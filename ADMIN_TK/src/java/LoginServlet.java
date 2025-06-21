import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Query to check if the user exists
        String query = "SELECT * FROM admin WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            // Check if user exists
            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");
                int roleId = rs.getInt("role_id");

                // Verify password (using SHA-256 hash comparison)
                if (verifyPassword(password, storedHashedPassword)) {
                    HttpSession session = request.getSession();
                    session.setAttribute("username", username);
                    session.setAttribute("role_id", roleId);  // Store role_id for RBAC

                    // Redirect based on user role
                    if (roleId == 1) {
                        // Admin (Full access)
                        response.sendRedirect("dashboard.jsp");
                    } else if (roleId == 2) {
                        // Manager (Add, Edit, Delete only)
                        response.sendRedirect("AdminAddEditDeletedashboard.jsp");
                    } else if (roleId == 3) {
                        // EditDelete (EditDelete only)
                        response.sendRedirect("AdminEditDeleteDashboard.jsp");
                    } else if (roleId == 4) {
                        // EditDelete (EditDelete only)
                        response.sendRedirect("AdminViewerDashboard.jsp");
                    } else {
                        // Invalid role
                        response.sendRedirect("login.jsp?error=invalid_role");
                    }
                } else {
                    // Invalid password
                    response.sendRedirect("login.jsp?error=invalid_credentials");
                }
            } else {
                // User not found
                response.sendRedirect("login.jsp?error=invalid_credentials");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during login", e);
            response.sendRedirect("login.jsp?error=true"); // generic error message
        }
    }

    // Hashing function using SHA-256
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(password.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));  // Convert to hex
        }
        return hexString.toString();
    }

    // Password verification logic
    private boolean verifyPassword(String enteredPassword, String storedHashedPassword) {
        try {
            String enteredHashedPassword = hashPassword(enteredPassword);  // Hash the entered password
            return enteredHashedPassword.equals(storedHashedPassword); // Compare the hashes
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
    }
}
