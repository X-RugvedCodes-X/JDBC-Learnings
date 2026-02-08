package com.dada.transactionhandling;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import io.github.cdimascio.dotenv.Dotenv;

public class Main {
  private static final Dotenv dotenv   = Dotenv.load();
  private static final String url      = dotenv.get("DB_URL"); 
  private static final String username = dotenv.get("DB_USER"); 
  private static final String password = dotenv.get("DB_PASSWORD"); 

  public static void main(String[] args) {
    System.out.println("hello, transaction handling !");
    
    String debitQuery   = "UPDATE accounts SET balance = balance - ? WHERE accountNumber = ?";
    String creditQuery  = "UPDATE accounts SET balance = balance + ? WHERE accountNumber = ?";

    try (Connection connection = DriverManager.getConnection(url, username, password);
         PreparedStatement debitPreparedStatement  = connection.prepareStatement(debitQuery); 
         PreparedStatement creditPreparedStatement = connection.prepareStatement(creditQuery);
         Scanner sc = new Scanner(System.in)) {
      
      connection.setAutoCommit(false);
      System.out.print("Enter Amount to make Transaction: ");
      if(!sc.hasNextDouble()) {
        System.out.println("Amount Must be a Numeric");
        return;
      }
      double amount = sc.nextDouble();

      System.out.print("Enter Account Number to Debit: ");
      if(!sc.hasNextInt()) {
        System.out.println("Account Number Must be a Numeric");
        return;
      }
      int debitAccountNumber = sc.nextInt();

      System.out.print("Enter Account Number to Credit: ");
      if(!sc.hasNextInt()) {
        System.out.println("Account Number Must be a Numeric");
        return;
      }
      int creditAccountNumber = sc.nextInt();

      debitPreparedStatement.setDouble(1, amount);
      debitPreparedStatement.setInt(2, debitAccountNumber);
      creditPreparedStatement.setDouble(1, amount);
      creditPreparedStatement.setInt(2, creditAccountNumber);

      if (!hasSufficientBalance(connection, debitAccountNumber, amount)) {
        System.out.println("Insufficient balance!");
        connection.rollback();
        System.out.println("Transaction Failure !");
        return;
      }
      int affectedRowsFordebitPreparedStatement   = debitPreparedStatement.executeUpdate();
      int affectedRowsForcreditPreparedStatement  = creditPreparedStatement.executeUpdate();
      
      if(affectedRowsFordebitPreparedStatement == 0 || affectedRowsForcreditPreparedStatement == 0) {
        System.out.println("Invalid account number provided.");
        connection.rollback();
        return;
      }
      connection.commit();
      System.out.println("Transaction Successful!");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private static boolean hasSufficientBalance(Connection connection, int accountNumber, double amount) throws SQLException {
    String query = "SELECT balance FROM accounts WHERE accountNumber = ?";
    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      preparedStatement.setInt(1, accountNumber);
      ResultSet resultSet = preparedStatement.executeQuery();

      if(resultSet.next()) {
        double currentBalance = resultSet.getDouble("balance");
        return currentBalance >= amount;
      } else {
        return false;
      }
    }
  }
}
