/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author User
 */
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private Connection getConnection() throws SQLException {
        // Example of obtaining a database connection.
        // Replace with your actual connection logic.
        String url = "jdbc:mysql://localhost:3306/your_db_name";
        String username = "root";
        String password = "password";
        return DriverManager.getConnection(url, username, password);
    }

    // Add a new user
    public boolean addUser(User user) {
        // Check if the email already exists in the database
        if (isEmailExists(user.getEmail())) {
            return false; // Email is duplicated
        }

        String query = "INSERT INTO users (name, email, position, department) VALUES (?, ?, ?, ?)";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPosition());
            statement.setString(4, user.getDepartment());

            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Check if an email already exists in the database
    private boolean isEmailExists(String email) {
        String query = "SELECT 1 FROM users WHERE email = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next(); // Returns true if email exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Update an existing user
    public boolean updateUser(User user) {
        String query = "UPDATE users SET name = ?, email = ?, position = ?, department = ? WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPosition());
            statement.setString(4, user.getDepartment());
            statement.setInt(5, user.getId());

            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete a user by ID
    public boolean deleteUser(int userId) {
        String query = "DELETE FROM users WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);

            int rowsDeleted = statement.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Retrieve all users
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users";

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                User user = new User();
                user.setId(resultSet.getInt("id"));
                user.setName(resultSet.getString("name"));
                user.setEmail(resultSet.getString("email"));
                user.setPosition(resultSet.getString("position"));
                user.setDepartment(resultSet.getString("department"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // Retrieve a user by ID
    public User getUserById(int userId) {
        User user = null;
        String query = "SELECT * FROM users WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    user = new User();
                    user.setId(resultSet.getInt("id"));
                    user.setName(resultSet.getString("name"));
                    user.setEmail(resultSet.getString("email"));
                    user.setPosition(resultSet.getString("position"));
                    user.setDepartment(resultSet.getString("department"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }
}
