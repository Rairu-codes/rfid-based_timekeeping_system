<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="jakarta.servlet.http.HttpSession"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard | Torres Technology Corporation</title>
    <style>
        /* Same styles as before */
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
        
        /* Styles for the clock and date-time display */
        .clock-container {
            margin-top: 20px;
            text-align: center;
            background-color: #004f7c;
            color: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }
        .clock {
            font-size: 3em;
            font-weight: bold;
        }
        .date {
            font-size: 1.5em;
            margin-top: 10px;
        }
    </style>
    <script>
        // Function to update the current date and time
        function updateDateTime() {
            var dateTimeElement = document.getElementById("dateTime");
            var now = new Date();
            var date = now.toLocaleDateString();
            var time = now.toLocaleTimeString();
            dateTimeElement.innerHTML = time;
            document.getElementById("date").innerHTML = "Current Date: " + date;
        }

        // Update every second
        setInterval(updateDateTime, 1000);

        // Call it immediately to set the initial time
        window.onload = updateDateTime;
    </script>
</head>
<body>

    <header>
        <h1>TIMEKEEPING MANAGEMENT</h1>
        <p>Manager Dashboard</p>
    </header>
    <div class="container">
        <div class="nav-links">
            <!-- Add the "active" class here for the Dashboard page link -->
            <a href="AdminEditDeleteDashboard.jsp" class="active">Dashboard</a>
            <a href="AdminEditDelete">Manage Users</a>
            <a href="LogoutServlet">Logout</a> <!-- Logout link -->
        </div>
    </div>

    <!-- Date and Time Section -->
    <div class="clock-container">
        <div class="clock" id="dateTime"></div>
        <div class="date" id="date"></div>
    </div>

    <!-- Display Error or Success Messages -->
    <c:if test="${not empty errorMessage}">
        <div class="message-box error">
            <span id="errorMessage">${errorMessage}</span>
            <button class="close-btn" onclick="closeMessageBox(this)">×</button>
        </div>
    </c:if>

    <c:if test="${not empty message}">
        <div class="message-box success">
            <span id="successMessage">${message}</span>
            <button class="close-btn" onclick="closeMessageBox(this)">×</button>
        </div>
    </c:if>

    <div class="footer">
        <p></p>
    </div>
</body>
</html>
