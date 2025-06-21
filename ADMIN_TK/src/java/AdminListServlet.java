import jakarta.servlet.RequestDispatcher;
import java.io.IOException;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;

@WebServlet("/AdminListServlet")
public class AdminListServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:mysql://10.44.0.56:3306/TK_DB"; // Change to your database URL
    private static final String DB_USER = "Host";  // Replace with your MySQL username
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

    // Fetch admin details from the database
    private List<Map<String, Object>> fetchAdmins() throws SQLException {
        List<Map<String, Object>> adminList = new ArrayList<>();

        String query = "SELECT id, role_id, username, created_at FROM admin";

        // Execute the query
        try (Connection conn = connectToDatabase(); PreparedStatement stmt = conn.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Create a map to hold each admin's data
                    Map<String, Object> admin = new HashMap<>();
                    admin.put("id", rs.getInt("id"));
                    admin.put("role_id", rs.getInt("role_id"));
                    admin.put("username", rs.getString("username"));
                    admin.put("created_at", rs.getTimestamp("created_at"));

                    adminList.add(admin);
                }
            }
        }
        return adminList;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Fetch the admin data
            List<Map<String, Object>> adminList = fetchAdmins();

            // Set admin data as a request attribute
            request.setAttribute("adminList", adminList);

            // Forward the request to admin.jsp
            RequestDispatcher dispatcher = request.getRequestDispatcher("admin_list.jsp");
            dispatcher.forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching admin data from the database.");
        }
    }
}
