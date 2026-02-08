package com.dada.batchprocessing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

import io.github.cdimascio.dotenv.Dotenv;

public class Main {

  private static final Dotenv dotenv   = Dotenv.load();
  private static final String url      = dotenv.get("DB_URL");
  private static final String username = dotenv.get("DB_USER");
  private static final String password = dotenv.get("DB_PASSWORD");
  public static void main(String[] args) throws SQLException {
    System.out.println("hello, batch processing !");

    String query = "INSERT INTO students(name, age, marks) VALUES(?, ?, ?)";
    Connection connection = null;
    try (Scanner sc = new Scanner(System.in)) {
      connection = DriverManager.getConnection(url, username, password);
      connection.setAutoCommit(false);    // * Most Important
      
      try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
        int batchSize = 50, count = 0;
        char choice = '\0';
      
        System.out.println("# Insert Student Data into Table");
        do {
          System.out.print("Enter Student's Name: ");
          String name = sc.nextLine().trim();
          if(name.isEmpty()) {
            System.out.println("Name Cannot be Empty");
            continue;
          }
          System.out.print("Enter Student's Age: ");
          if (!sc.hasNextInt()) {
            System.out.println("Age must be a number");
            sc.nextLine();
            continue;
          }
          int age = sc.nextInt();
          if (age <= 10) {
            System.out.println("Enter Age Greater Than 10");
            sc.nextLine();
            continue;
          }
          System.out.print("Enter Student's Marks: ");
          double marks = sc.nextDouble();
          if(marks <= 0.0) {
            System.out.println("Don't Enter Negative Marks");
            sc.nextLine();
            continue;
          }

          preparedStatement.setString(1, name);
          preparedStatement.setInt(2, age);
          preparedStatement.setDouble(3, marks);

          preparedStatement.addBatch();
          count++;

          if(count % batchSize == 0) {
            int[] rowsAffectedCheck = preparedStatement.executeBatch();
            for (int i = 0; i < rowsAffectedCheck.length; ++i) {
              if(rowsAffectedCheck[i] == PreparedStatement.EXECUTE_FAILED)
                System.out.println("Query: " + i + " not Executed Successfully.");
            }
            preparedStatement.clearBatch();
          }
          System.out.print("Want to Enter More Students to Database ? (Y/N): ");
          choice = Character.toUpperCase(sc.next().trim().charAt(0));
          sc.nextLine();    // * Clearing the Buffer by consuming leftover New line
          while(choice != 'Y' && choice != 'N') {
            System.out.print("Enter Either 'Y' or 'N' only: ");
            choice = Character.toUpperCase(sc.next().trim().charAt(0));
            sc.nextLine();
          }
        } while (choice != 'N');
        preparedStatement.executeBatch();   // * Processing LeftOver Batch
        System.out.println("Batch of All Entered Student Executed.");
        connection.commit();                // * Finally Commiting
        System.out.println("Inserted All Entered Students Successfully.");
      }
    } catch (SQLException e) {
      if (connection != null) 
        connection.rollback();
      e.printStackTrace();
    }
  }
}
