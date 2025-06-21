<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login</title>
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
        .login-container {
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
        input {
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
            font-size: 14px;
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
    <div class="login-container">
        <h1>TIMEKEEPING</h1>
        <form action="LoginServlet" method="POST">
            <input type="text" name="username" placeholder="Username" required><br>
            <input type="password" name="password" placeholder="Password" required><br>
            <input type="submit" value="LOGIN">
        </form>
        <% if(request.getParameter("error") != null) { %>
            <p class="error-message">Invalid username or password. Please try again.</p>
        <% } %>
        
        <a href="CreateAdminAccount.jsp" class="create-admin-link">CREATE ADMIN ACCOUNT</a>
    </div>
</body>
</html>
