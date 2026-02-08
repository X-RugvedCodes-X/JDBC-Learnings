package com.dada.jdbcintro;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import io.github.cdimascio.dotenv.Dotenv;

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

    try {
      Connection connection = DriverManager.getConnection(url, username, password);
      System.out.println("Connected to Database Successfully ....");
      Statement statement   = connection.createStatement();
      String    Query       = "SELECT * FROM students";
      ResultSet resultSet   = statement.executeQuery(Query);

      while (resultSet.next()) {
        int id        = resultSet.getInt("id");
        String name   = resultSet.getString("name");
        int age       = resultSet.getInt("age");
        double marks  = resultSet.getDouble("marks");
        
        System.out.println("Student -> id: " + id + ", Name: " + name + ", Age: " + age + ", Marks: " + marks);
      }
      resultSet.close();
      statement.close();
      connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    } 
    System.out.println("Connection Closed !");
  }
} 
