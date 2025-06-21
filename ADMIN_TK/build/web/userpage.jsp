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
        <p>Manage Users</p>
    </header>

    <div class="nav-links">
        <a href="dashboard.jsp">Dashboard</a>
        <a href="AdminListServlet">Admin List</a>
        <a href="AddUserServlet" class="active">Manage Users</a>
        <a href="LogsServlet">Employee Time Logs</a>
        <a href="ErrorLogsServlet">Error Logs</a>
        <a href="ReportsServlet">Reports and Analytics</a>
        <a href="LogoutServlet">Logout</a>
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

    <!-- Form for adding users -->
    <div class="container">
        <form action="AddUserServlet" method="POST" onsubmit="return confirmAddUser();">
            <h3>Add User</h3>
            <input type="text" name="name" placeholder="Name" required>
            <input type="text" name="rfid_tag" placeholder="RFID Tag" required>
            <input type="email" name="email" placeholder="Email" required>
             <input type="text" name="position" placeholder="Position" required>
             <input type="text" name="department" placeholder="Department" required>
             <!-- Dropdown for Shift (Day or Night) -->
             <select name="shift" id="shift" required>
             <option value="" disabled selected>Select Shift</option>
              <option value="Day">Day</option>
              <option value="Night">Night</option>
    </select>

            
            <input type="submit" value="Add User">
            <input type="hidden" name="action" value="add">
        </form>
        
          <!-- Search and Filter Form -->
        <form action="AddUserServlet" method="GET">
            <h3>Search and Filter Users</h3>
            <input type="text" name="emp_id" placeholder="Search by Employee ID" value="${param.emp_id}">
            <input type="submit" value="Search">
            <br><br>
            
            <input type="text" name="position" placeholder="Sort by Position" value="${param.position}">
            <input type="text" name="department" placeholder="Sort by Department" value="${param.department}">
            <input type="submit" value="Apply Filters">
        </form>


        <!-- User List Table -->
        <h2>User List</h2>
        <div class="table-container">
            <table>
                <thead>
                    <tr>
                        <th>Employee ID</th>
                        <th>RFID Tag</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Position</th>
                        <th>Department</th>
                        <th>Shift Type</th>
                        <th>Time Stamp</th>
                        <th>Modified by</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <%  
                        List<Map<String, Object>> userList = (List<Map<String, Object>>) request.getAttribute("userList");
                        if (userList != null && !userList.isEmpty()) {
                            for (Map<String, Object> user : userList) {
                    %>
                    <tr>
                        <td><%= user.get("emp_id") %></td>
                        <td><input type="text" value="<%= user.get("rfid_tag") %>" id="rfid_tag<%= user.get("user_id") %>" class="editable" disabled /></td>
                        <td><input type="text" value="<%= user.get("name") %>" id="name_<%= user.get("user_id") %>" class="editable" disabled /></td>
                        <td><input type="email" value="<%= user.get("email") %>" id="email_<%= user.get("user_id") %>" class="editable" disabled /></td>
                        <td><input type="text" value="<%= user.get("position") %>" id="position_<%= user.get("user_id") %>" class="editable" disabled /></td>
                        <td><input type="text" value="<%= user.get("department") %>" id="department_<%= user.get("user_id") %>" class="editable" disabled /></td><!-- comment -->
                        <!-- New Shift column with a dropdown -->
                        <td>
                        <select id="shift_type<%= user.get("user_id") %>" class="editable" disabled>
                         <option value="Day" <%= "Day".equals(user.get("shift_type")) ? "selected" : "" %>>Day</option>
                         <option value="Night" <%= "Night".equals(user.get("shift_type")) ? "selected" : "" %>>Night</option>
                        </select>
                        </td>
                        <td><%= user.get("created_at") %></td>
                        <td><%= user.get("modified_by") != null ? user.get("modified_by") : "N/A" %></td>
                        <td>
                            <a href="javascript:void(0);" onclick="toggleEdit(<%= user.get("user_id") %>)" class="action-btn edit-btn" id="edit_<%= user.get("user_id") %>">Edit</a> |
                            <a href="AddUserServlet?action=delete&id=<%= user.get("user_id") %>" onclick="return confirm('Are you sure you want to delete this user?');" class="action-btn delete-btn">Delete</a>
                        </td>
                    </tr>
                    <%  
                            }
                        } else {
                    %>
                    <tr><td colspan="10">No users found</td></tr>
                    <%  
                        }
                    %>
                </tbody>
            </table>
        </div>
    </div>

    <script>
        function toggleEdit(userId) {
            var editBtn = document.getElementById("edit_" + userId);
            var rfidInput = document.getElementById("rfid_tag" + userId);
            var nameInput = document.getElementById("name_" + userId);
            var emailInput = document.getElementById("email_" + userId);
            var positionInput = document.getElementById("position_" + userId);
            var departmentInput = document.getElementById("department_" + userId);
            var shiftInput = document.getElementById("shift_type" + userId);

            if (editBtn.innerHTML === "Edit") {
                // Change the button to "Save"
                editBtn.innerHTML = "Save";
                editBtn.classList.remove("edit-btn");
                editBtn.classList.add("save-btn");

                // Enable the inputs for editing
                rfidInput.disabled = false;
                nameInput.disabled = false;
                emailInput.disabled = false;
                positionInput.disabled = false;
                departmentInput.disabled = false;
                shiftInput.disabled = false;
            } else {
                // Confirm the update
                if (confirm("Are you sure you want to update the details?")) {
                    // Change the button back to "Edit"
                    editBtn.innerHTML = "Edit";

                    // Disable the inputs after saving
                    rfidInput.disabled = true;
                    nameInput.disabled = true;
                    emailInput.disabled = true;
                    positionInput.disabled = true;
                    departmentInput.disabled = true;
                    shiftInput.disabled = true;

                    // Save the user data via AJAX
                    saveUser(userId);
                }
            }
        }

        function saveUser(userId) {
            var rfid_tag = document.getElementById("rfid_tag" + userId).value;
            var name = document.getElementById("name_" + userId).value;
            var email = document.getElementById("email_" + userId).value;
            var position = document.getElementById("position_" + userId).value;
            var department = document.getElementById("department_" + userId).value;
            var shift_type = document.getElementById("shift_type" + userId).value;

            var xhr = new XMLHttpRequest();
            xhr.open("POST", "AddUserServlet", true);
            xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");

            // Sending the updated user details in the request
            xhr.send("action=edit&id=" + userId + "&rfid_tag=" + encodeURIComponent(rfid_tag) + "&name=" + encodeURIComponent(name) + "&email=" + encodeURIComponent(email) + "&position=" + encodeURIComponent(position) + "&department=" + encodeURIComponent(department) + "&shift_type=" + encodeURIComponent(shift_type));

            xhr.onload = function() {
                if (xhr.status == 200) {
                    alert("User updated successfully!");
                    location.reload(); // Reload the page to reflect changes
                } else {
                    alert("Error updating user!");
                    location.reload(); // Reload the page to reflect changes
                }
            };
        }

            function confirmAddUser() {
                
                return confirm("Are you sure you want to add this user?");

            }

        function closeMessageBox(button) {
            button.parentElement.style.display = "none";
        }
    </script>
</body>
</html>
