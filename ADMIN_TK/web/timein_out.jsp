<%-- 
    Document   : Timein_out
    Created on : 9 Jan 2025, 2:38:21 PM
    Author     : User
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Timekeeping System</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: powderblue;
            margin: 0;
            padding: 0;
        }
        header {
            background-color: #ff4d4d;
            padding: 15px;
            text-align: center;
            color: white;
        }
        .container {
            margin: 30px;
            text-align: center;
        }
        button {
            padding: 15px 30px;
            font-size: 18px;
            color: white;
            background-color: #ff4d4d;
            border: none;
            border-radius: 5px;
            cursor: pointer;
        }
        button:hover {
            background-color: #e60000;
        }
        .back-btn {
            background-color: #ff4d4d;
            color: white;
            padding: 10px 20px;
            border: none;
            cursor: pointer;
            text-decoration: none;
            margin-top: 20px;
        }
        .back-btn:hover {
            background-color: #e60000;
        }
        .message {
            margin: 20px;
            font-size: 18px;
            color: green;
        }
        .error {
            color: red;
        }
    </style>
</head>
<body>
    <header>
        <h1>Employee Timekeeping</h1>
    </header>
    <div class="container">
        <h2>Welcome, ${employee.name}</h2>

        <!-- Show Message -->
        <c:if test="${param.message != null}">
            <div class="message <c:if test="${param.message == 'Error'}">error</c:if>">
                <c:choose>
                    <c:when test="${param.message == 'Success'}">
                        Time logged successfully!
                    </c:when>
                    <c:when test="${param.message == 'Error'}">
                        Error logging time.
                    </c:when>
                    <c:otherwise>
                        Invalid request.
                    </c:otherwise>
                </c:choose>
            </div>
        </c:if>

        <!-- Time In / Time Out Buttons -->
        <form action="LogTimeServlet" method="POST">
            <input type="hidden" name="user_id" value="${employee.id}">
            <input type="hidden" name="timestamp" value="<%= new java.util.Date() %>">
            <button type="submit" name="action" value="Time In">Time In</button>
            <button type="submit" name="action" value="Time Out">Time Out</button>
        </form>

        <!-- Back to Dashboard Button -->
        <a href="dashboard.jsp" class="back-btn">Back to Dashboard</a>
    </div>
</body>
</html>

