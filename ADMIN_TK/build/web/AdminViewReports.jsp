<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.text.SimpleDateFormat" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Users | Torres Technology Corporation</title>
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: #f4f6f9;
            margin: 0;
            padding: 0;
            color: #333;
        }
        header {
            background-color: #004f7c;
            padding: 20px;
            text-align: center;
            color: white;
        }
        header h1 {
            font-size: 2em;
            margin: 0;
        }
        header p {
            font-size: 1.2em;
            margin-top: 5px;
        }
        .nav-links {
            display: flex;
            justify-content: center;
            gap: 20px;
            background-color: #ffffff;
            padding: 15px 0;
        }
        .nav-links a {
            font-size: 1.1em;
            text-decoration: none;
            color: #004f7c;
            padding: 10px 20px;
            border-radius: 5px;
            border: 2px solid #004f7c;
            transition: all 0.3s;
        }
        .nav-links a:hover {
            background-color: #004f7c;
            color: white;
            border-color: #003e5c;
        }
        .nav-links a.active {
            background-color: #003e5c;
            color: white;
            border-color: #002f47;
        }
        .container {
            margin: 30px auto;
            max-width: 1200px;
            background-color: white;
            padding: 20px;
            border-radius: 10px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }
        h2, h3 {
            color: #004f7c;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }
        table, th, td {
            border: 1px solid #ddd;
        }
        th, td {
            padding: 12px;
            text-align: left;
        }
        th {
            background-color: #004f7c;
            color: white;
        }
        td {
            background-color: #fafafa;
        }
        form {
            margin-bottom: 30px;
        }
        input[type="text"], input[type="email"], input[type="submit"], select {
            padding: 8px 12px;
            margin: 8px 0;
            font-size: 1em;
            width: 200px;
            border-radius: 5px;
            border: 1px solid #ccc;
            box-sizing: border-box;
        }
        input[type="submit"] {
            background-color: #004f7c;
            color: white;
            border: none;
            cursor: pointer;
        }
        input[type="submit"]:hover {
            background-color: #003e5c;
        }
        .back-btn {
            background-color: #004f7c;
            color: white;
            padding: 10px 20px;
            border: none;
            cursor: pointer;
            text-decoration: none;
            margin-top: 20px;
            border-radius: 5px;
        }
        .back-btn:hover {
            background-color: #003e5c;
        }
        .message-box {
            padding: 15px;
            margin-bottom: 20px;
            border-radius: 5px;
            display: none; /* Initially hidden */
            justify-content: space-between;
            align-items: center;
            font-size: 16px;
        }
        .error {
            background-color: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
        .success {
            background-color: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        .close-btn {
            cursor: pointer;
            font-size: 18px;
            background: none;
            border: none;
            color: inherit;
        }
        .table-container {
            max-height: 400px;
            overflow-y: auto;
        }
        thead th {
            position: sticky;
            top: 0;
            background-color: #004f7c;
            color: white;
            z-index: 2;
        }
        .action-btn {
            font-size: 0.9em;
            padding: 5px 10px;
            border-radius: 5px;
            text-decoration: none;
            cursor: pointer;
        }
         .edit-btn {
            background-color: #007bff; /* Blue for Edit */
            color: white;
            border: 1px solid #007bff;
            margin-right: 10px;
        }
        .edit-btn:hover {
            background-color: #0056b3;
            border-color: #0056b3;
        }
        .save-btn {
            background-color: #28a745; /* Green for Save */
            color: white;
            border: 1px solid #28a745;
        }
        .save-btn:hover {
            background-color: #218838;
            border-color: #218838;
        }
        .delete-btn {
            background-color: #f44336;
            color: white;
            border: 1px solid #f44336;
        }
        .delete-btn:hover {
            background-color: #e53935;
            border-color: #e53935;
        }
    </style>
</head>
<body>
    <header>
        <h1>TIMEKEEPING MANAGEMENT</h1>
        <p>Reports</p>
    </header>

    <!-- Navigation Links -->
    <div class="nav-links">
        <a href="AdminViewerashboard.jsp">Dashboard</a>
        <a href="AdminViewEmployees">Employee List</a>
        <a href="AdminViewLogs">Employee Time Logs</a>
        <a href="AdminViewErrorLogs">Error Logs</a>
        <a href="AdminViewReports" class="active">Reports and Analytics</a>
        <a href="LogoutServlet">Logout</a>
    </div>
<!-- Search and Filter Form -->
<div class="container">
<form action="ReportsServlet" method="GET">
        <h3>Search and Filter Users</h3>
        <input type="text" name="emp_id" placeholder="Search by Employee ID" value="${param.emp_id}">
        <input type="submit" value="Search">
        <br><br>
        
        <input type="text" name="position" placeholder="Sort by Position" value="${param.position}">
        <input type="text" name="department" placeholder="Sort by Department" value="${param.department}">
        <input type="submit" value="Apply Filters">
        </form>
    </div>

<!-- Reports Table -->
<div class="container">
    <h2>Employee Reports</h2>
    <table>
        <thead>
            <tr>
                <th>Employee ID</th>
                <th>Name</th>
                <th>Position</th>
                <th>Department</th>
                <th>Number of Late</th>
                <th>Regular Hours Worked</th>
                <th>Overtime Hours Worked</th>
                <th>Number of Early Out</th>
                <th>Number of Absents</th>
            </tr>
        </thead>
        <tbody>
            <%  
                // Get the aggregatedLogs from the request attribute
                List<Map<String, Object>> aggregatedLogs = (List<Map<String, Object>>) request.getAttribute("aggregatedLogs");
                if (aggregatedLogs != null && !aggregatedLogs.isEmpty()) {
                    for (Map<String, Object> log : aggregatedLogs) {
            %>
            <tr>
                <td><%= log.get("emp_id") %></td>
                <td><%= log.get("name") %></td>
                <td><%= log.get("position") %></td>
                <td><%= log.get("department") %></td>
                <td><%= log.get("late_count") != null ? log.get("late_count") : "None" %></td>
                <td><%= log.get("regular_hours") != null ? log.get("regular_hours") : "None" %></td>
                <td><%= log.get("overtime_hours") != null ? log.get("overtime_hours") : "None" %></td>
                <td><%= log.get("early_out_count") != null ? log.get("early_out_count") : "None" %></td>
                <td><%= log.get("absent_count") != null ? log.get("absent_count") : "None" %></td>
            </tr>
            <%  
                    }
                } else {
            %>
            <tr><td colspan="9" class="no-data">No logs found</td></tr>
            <%  
                }
            %>
        </tbody>
    </table>
</div>
</body>
</html>
