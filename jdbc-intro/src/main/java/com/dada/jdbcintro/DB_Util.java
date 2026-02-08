package com.dada.jdbcintro;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import io.github.cdimascio.dotenv.Dotenv;

@SuppressWarnings("unused")
public class DB_Util {
  public static final Dotenv dotenv   = Dotenv.load();
  public static final String url      = dotenv.get("DB_URL"); 
  public static final String username = dotenv.get("DB_USER"); 
  public static final String password = dotenv.get("DB_PASSWORD");

  // ^ Steps Are: 
  // * 1. Load the Class (Not Required in Latest JDBC Versions - JDBC 4.0 or newer) 
  // * 2. Make a Connection
  // * 3. Obtain a Statement
  // * 4. Execute The Query
  // * 5. Close the Connection

  // * We Use Thin Driver or Direct to Database Pure Java Driver as this is most efficient
  public static void connectToDB() {
    System.out.println("Connecting to Database ....");
    // * 1. Loading the Driver
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    // * Data Retrieval Using SELECT Statement
    // try {
    //   Connection connection = DriverManager.getConnection(url, username, password);
    //   System.out.println("Connected to Database Successfully ....");
    //   Statement statement   = connection.createStatement();
    //   String    Query       = "SELECT * FROM students";
    // * When we retrieve data from database, We use executeQuery method which returns a ResultSet.
    //   ResultSet resultSet   = statement.executeQuery(Query);

    //   while (resultSet.next()) {
    //     int id        = resultSet.getInt("id");
    //     String name   = resultSet.getString("name");
    //     int age       = resultSet.getInt("age");
    //     double marks  = resultSet.getDouble("marks");
        
    //     System.out.println("Student -> id: " + id + ", Name: " + name + ", Age: " + age + ", Marks: " + marks);
    //   }
    //   resultSet.close();
    //   statement.close();
    //   connection.close();
    // } catch (SQLException e) {
    //   e.printStackTrace();
    // } 
    // System.out.println("Connection Closed !");

    // * Inserting Data into Table Using INSERT INTO... statment
    // try {
    //   Connection connection = DriverManager.getConnection(url, username, password);
    //   System.out.println("Connected to Database Successfully ....");
      // Statement statement   = connection.createStatement();
      // String    Query       = String.format("INSERT INTO students(name, age, marks) VALUES('%s', %d, %f)", "Dada", 22, 80.85);
      // * Now for Inserting data we use executeUpdate Method from the Statement Interface and it returns an integer mentioning how many rows affected
      // int rowsAffected    = statement.executeUpdate(Query);
      // if (rowsAffected > 0) 
      //   System.out.println("Data Inserted Successfully !");
      // else
      //   System.out.println("Problem in Data Insertion !");
      // statement.close();
      // connection.close();

      // * The same Above Operation Can be done using PreparedStatement Interface for the below Reasons: 
      // * 1. Prevents SQL Injection - Values are sent separately from the SQL.
      // * 2. No manual quoting - We dont worry about ' or date formats
      // * 3. Correct datatype handling - setInt, setDouble, etc.
      // * 4. Faster for repeated queries - DB can reuse the execution plan.
    //   String Query = "INSERT INTO students(name, age, marks) VALUES(?, ?, ?)";
    //   PreparedStatement preparedStatement = connection.prepareStatement(Query);
    //   preparedStatement.setString(1, "Sachin");
    //   preparedStatement.setInt(2, 32);
    //   preparedStatement.setDouble(3, 90.85);

    //   int rowsAffected = preparedStatement.executeUpdate();
    //   if (rowsAffected > 0) 
    //     System.out.println("Data Inserted Successfully !");
    //   else
    //     System.out.println("Problem in Data Insertion !");      
    //   preparedStatement.close();
    //   connection.close();
    // } catch (SQLException e) {
    //   e.printStackTrace();
    // } 

    // * Updating Data in table using, UPDATE table_name SET ...
    // try {
    //   Connection connection = DriverManager.getConnection(url, username, password);
    //   String queryForDataUpdation = "UPDATE students SET marks = ? WHERE id = ?";
    //   PreparedStatement preparedStatement = connection.prepareStatement(queryForDataUpdation);
    //   preparedStatement.setDouble(1, 95.75);
    //   preparedStatement.setInt(2, 3);

    //   int rowsAffected = preparedStatement.executeUpdate();
    //   System.out.println(rowsAffected > 0 ? "Data Updated Successfully !" : "Problem in Data Updation !");
    //   preparedStatement.close();
    //   connection.close();
    // } catch(SQLException e) {
    //   e.printStackTrace();
    // }

    // * Deleting Data from a Table using DELETE FROM table_name
    // ^ Using try with Resources as this is considered as the standard method nowadays (No need of connection closing or resource cleanup, no finally needed)
    // try (Connection connection = DriverManager.getConnection(url, username, password)) {
    //   String queryForDeletionOfData = "DELETE FROM students WHERE id = ?";
    //   try (PreparedStatement preparedStatement = connection.prepareStatement(queryForDeletionOfData)) {
    //     preparedStatement.setInt(1, 3);
    //     int rowsAffected = preparedStatement.executeUpdate();
    //     System.out.println(rowsAffected > 0 ? "Data Deleted Successfully !" : "Problem in Data Deletion !");
    //   } catch (SQLException e) {
    //     e.printStackTrace();
    //   }
    // } catch (SQLException e) {
    //   e.printStackTrace();
    // }

    // * More Standard Way, Using two Execption Statement in try block Simultaneously
    String queryForDeletionOfData = "DELETE FROM students WHERE id = ?";
    try (Connection connection = DriverManager.getConnection(url, username, password);
        PreparedStatement preparedStatement = connection.prepareStatement(queryForDeletionOfData)) {
      preparedStatement.setInt(1, 3);
      int rowsAffected = preparedStatement.executeUpdate();
      System.out.println(rowsAffected > 0 ? "Data Deleted Successfully !" : "Problem in Data Deletion !");
    } catch (SQLException e) {
      e.printStackTrace();
    }
    System.out.println("Connection Closed !");
  }
} 
