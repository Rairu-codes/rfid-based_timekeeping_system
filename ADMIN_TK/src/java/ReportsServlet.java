import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/ReportsServlet")
public class ReportsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();  // Assuming you have a DatabaseConnection utility class
    }

    private String calculateTimeOutStatus(Timestamp timeOut) {
        if (timeOut == null) {
            return "Absent"; // No time-out entry means Absent
        }

        // Extract the hour and minute from the timeOut timestamp
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(timeOut.getTime());
        int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        int minute = calendar.get(java.util.Calendar.MINUTE);

        // Define the time limits based on your conditions
        if (hour >= 6 && hour < 10) {
            return "Time-Out Not Allowed (6:30 AM - 10:00 AM)";
        } else if (hour >= 10 && hour <= 15 && minute <= 30) {
            return "Early Out"; // Before 3:30 PM
        } else if (hour > 15 && hour <= 15 && minute <= 40) {
            return "Normal"; // Between 3:30 PM and 3:40 PM
        } else if (hour >= 16 && hour < 17) {
            return "Overtime"; // After 4 PM but before 5 PM
        } else {
            return "Unknown"; // Other cases
        }
    }

    private double[] calculateRegularAndOvertimeHours(Timestamp timeIn, Timestamp timeOut, String shiftType) {
        if (timeIn == null || timeOut == null) {
            return new double[]{0.0, 0.0};
        }

        long workedMillis = timeOut.getTime() - timeIn.getTime();
        double workedHours = workedMillis / (1000.0 * 60.0 * 60.0); // Convert to hours

        // Define regular shift start and end times
        java.util.Calendar regularStart = java.util.Calendar.getInstance();
        java.util.Calendar regularEnd = java.util.Calendar.getInstance();

        if (shiftType.equals("Day")) {
            regularStart.set(java.util.Calendar.HOUR_OF_DAY, 6);
            regularStart.set(java.util.Calendar.MINUTE, 30);
            regularEnd.set(java.util.Calendar.HOUR_OF_DAY, 15);
            regularEnd.set(java.util.Calendar.MINUTE, 30);
        } else if (shiftType.equals("Night")) {
            regularStart.set(java.util.Calendar.HOUR_OF_DAY, 18);
            regularStart.set(java.util.Calendar.MINUTE, 30);
            regularEnd.set(java.util.Calendar.HOUR_OF_DAY, 3);
            regularEnd.set(java.util.Calendar.MINUTE, 30);
        }

        // Subtract 1 hour for break time
        double regularHours = 8.0; // Initially assume 8 hours of regular work
        double overtimeHours = 0.0;

        // Check if the worked hours fall within the regular hours
        if (workedHours > 8) {
            overtimeHours = workedHours - 8.0;
            regularHours = 8.0;
        } else {
            regularHours = workedHours;
            overtimeHours = 0.0;
        }

        return new double[]{regularHours, overtimeHours};
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            connection = getConnection();

            // Get search and filter parameters from the request
            String empIdSearch = request.getParameter("emp_id");  // Search by Employee ID
            String sortPosition = request.getParameter("sortPosition");  // Sort by Position
            String sortDepartment = request.getParameter("sortDepartment");  // Sort by Department

            // Construct SQL query with conditions based on filters
            StringBuilder queryBuilder = new StringBuilder("SELECT u.emp_id, u.name, u.position, u.department, u.shift_type, l.time_in_status, l.time_out_status, l.time_in, l.time_out "
                                + "FROM logs l "
                                + "JOIN users u ON l.emp_id = u.emp_id WHERE 1=1 ");

            // Apply filter for emp_id if provided
            if (empIdSearch != null && !empIdSearch.isEmpty()) {
                queryBuilder.append(" AND u.emp_id LIKE ?");
            }

            // Apply filter for position if selected
            if (sortPosition != null && !sortPosition.isEmpty()) {
                queryBuilder.append(" AND u.position = ?");
            }

            // Apply filter for department if selected
            if (sortDepartment != null && !sortDepartment.isEmpty()) {
                queryBuilder.append(" AND u.department = ?");
            }

            // Apply sorting for position and department
            queryBuilder.append(" ORDER BY u.position, u.department");

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

            // This will store the aggregated data per employee (emp_id)
            Map<String, Map<String, Object>> employeeCategories = new HashMap<>();

            while (rs.next()) {
                String empId = rs.getString("emp_id");
                String name = rs.getString("name");
                String position = rs.getString("position");
                String department = rs.getString("department");
                String shiftType = rs.getString("shift_type");  // Get shift type from the database
                String timeInStatus = rs.getString("time_in_status");
                String timeOutStatus = rs.getString("time_out_status");
                Timestamp timeIn = rs.getTimestamp("time_in");
                Timestamp timeOut = rs.getTimestamp("time_out");

                // Initialize category counts for each employee if not yet initialized
                if (!employeeCategories.containsKey(empId)) {
                    Map<String, Object> categories = new HashMap<>();
                    categories.put("name", name);
                    categories.put("position", position);
                    categories.put("department", department);
                    categories.put("late", 0);           // Store as Integer
                    categories.put("absent", 0);         // Store as Integer
                    categories.put("overtime", 0);       // Store as Integer
                    categories.put("early_out", 0);      // Store as Integer
                    categories.put("regular_hours", 0.0); // Store regular hours worked as Double
                    categories.put("overtime_hours", 0.0); // Store overtime hours worked as Double
                    employeeCategories.put(empId, categories);
                }

                // Track categories: late, absent, overtime, early_out
                Map<String, Object> categories = employeeCategories.get(empId);

                // Count Absent: If time_in_status is "Absent" and time_out_status is "Absent" or null
                if ("Absent".equals(timeInStatus) && ("Absent".equals(timeOutStatus) || timeOutStatus == null)) {
                    categories.put("absent", (Integer) categories.get("absent") + 1);
                } else {
                    // Count Late: If time_in_status is "Late", increment late count
                    if ("Late".equals(timeInStatus)) {
                        categories.put("late", (Integer) categories.get("late") + 1);
                    }

                    // Calculate regular and overtime hours using the method
                    double[] hours = calculateRegularAndOvertimeHours(timeIn, timeOut, shiftType);
                    double regularHours = hours[0];
                    double overtimeHours = hours[1];

                    // Add to total regular and overtime hours
                    categories.put("regular_hours", (Double) categories.get("regular_hours") + regularHours);
                    categories.put("overtime_hours", (Double) categories.get("overtime_hours") + overtimeHours);
                }
            }

            // Prepare the aggregated data to be sent to the JSP
            List<Map<String, Object>> aggregatedLogs = new ArrayList<>();

            for (String empId : employeeCategories.keySet()) {
                Map<String, Object> categories = employeeCategories.get(empId);

                Map<String, Object> aggregatedData = new HashMap<>();
                aggregatedData.put("emp_id", empId);
                aggregatedData.put("name", categories.get("name"));
                aggregatedData.put("position", categories.get("position"));
                aggregatedData.put("department", categories.get("department"));
                aggregatedData.put("late_count", (Integer) categories.get("late") == 0 ? "None" : categories.get("late"));
                aggregatedData.put("absent_count", (Integer) categories.get("absent") == 0 ? "None" : categories.get("absent"));
                aggregatedData.put("regular_hours", (Double) categories.get("regular_hours") == 0.0 ? "None" : categories.get("regular_hours"));
                aggregatedData.put("overtime_hours", (Double) categories.get("overtime_hours") == 0.0 ? "None" : categories.get("overtime_hours"));
                aggregatedData.put("early_out_count", (Integer) categories.get("early_out") == 0 ? "None" : categories.get("early_out"));

                aggregatedLogs.add(aggregatedData);
            }

            // Set aggregatedLogs as request attribute to be used in the JSP page
            request.setAttribute("aggregatedLogs", aggregatedLogs);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred while fetching logs.");
        } finally {
            // Close connection and handle potential SQLException
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Forward the request to the reports.jsp page
        request.getRequestDispatcher("reports.jsp").forward(request, response);
    }
}
