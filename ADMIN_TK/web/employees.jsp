<%-- 
    Document   : employees
    Created on : 9 Jan 2025, 11:50:19 AM
    Author     : User
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!-- employees.jsp -->
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Employee List</title>
</head>
<body>
    <h1>Employee List</h1>
    <table>
        <thead>
            <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
            </tr>
        </thead>
        <tbody>
            <%
                List<EmployeeServlet.Employee> employees = (List<EmployeeServlet.Employee>) request.getAttribute("employees");
                for (EmployeeServlet.Employee employee : employees) {
            %>
            <tr>
                <td><%= employee.getName() %></td>
                <td><%= employee.getEmail() %></td>
                <td><%= employee.getRole() %></td>
            </tr>
            <%
                }
            %>
        </tbody>
    </table>
</body>
</html>

