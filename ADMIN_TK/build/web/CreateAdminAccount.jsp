<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="true" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>

<%
    // Get error or success messages from request parameters
    String errorMessage = request.getParameter("error");
    String successMessage = request.getParameter("success");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create Admin Account</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: powderblue;
            margin: 0;
            padding: 0;
            height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
        }
        .create-account-container {
            background-color: white;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0px 4px 10px rgba(0, 0, 0, 0.1);
            width: 300px;
            text-align: center;
        }
        h1 {
            color: #ff4d4d;
            margin-bottom: 20px;
        }
        input, select {
            width: 100%;
            padding: 10px;
            margin: 10px 0;
            border: 1px solid #ccc;
            border-radius: 5px;
            font-size: 16px;
        }
        input[type="submit"] {
            background-color: #ff4d4d;
            color: white;
            border: none;
            cursor: pointer;
            font-size: 1.1em;           /* Slightly larger font */
            font-weight: bold;          /* Make the text bold */
            text-align: center;   
        }
        input[type="submit"]:hover {
            background-color: #e60000;
        }
        .error-message {
            color: red;
        }
        .success-message {
            color: green;
        }
                .create-admin-link {
    background-color: #28a745;  /* Green background color */
    color: white;               /* White text */
    padding: 12px 24px;         /* Padding around the text */
    text-decoration: none;      /* Remove underline */
    border-radius: 5px;         /* Rounded corners */
    font-size: 1.1em;           /* Slightly larger font */
    font-weight: bold;          /* Make the text bold */
    text-align: center;         /* Center the text */
    display: inline-block;      /* Make it an inline-block so padding works */
    transition: all 0.3s ease;  /* Smooth transition for hover effects */
    
}

/* Hover effect */
.create-admin-link:hover {
    background-color: #218838;  /* Darker green on hover */
    color: #ffffff;             /* Ensure text is still white */
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);  /* Add a subtle shadow effect */
}
    </style>
</head>
<body>
    <div class="create-account-container">
        <h1>Create Admin Account</h1>

        <!-- Show error or success messages -->
        <% if (errorMessage != null) { %>
            <p class="error-message"><%= errorMessage %></p>
        <% } %>
        
        <% if (successMessage != null) { %>
            <p class="success-message"><%= successMessage %></p>
        <% } %>

        <!-- Form for creating admin account -->
        <form action="CreateAdminServlet" method="POST">
            <label for="username">Username:</label><br>
            <input type="text" id="username" name="username" required><br><br>

            <label for="password">Password:</label><br>
            <input type="password" id="password" name="password" required><br><br>

            <label for="roleId">Role:</label><br>
            <select id="roleId" name="roleId" required>
                <option value="1">Super Admin</option>
                <option value="2">Admin (Add User Only)</option>
                <option value="3">Admin (Edit/Delete User Only)</option>
                <option value="4">Viewer</option>
            </select><br><br>

            <input type="submit" value="Create Account">
        </form>
        
        <a href="login.jsp" class="create-admin-link">Go back to login</a>

    </div>
</body>
</html>
