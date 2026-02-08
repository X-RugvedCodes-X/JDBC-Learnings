package com.dada.jdbcsetup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.cdimascio.dotenv.Dotenv; 

public class Main {
  public static void main(String[] args) {
    System.out.println("hello, jdbc");

    Dotenv dotenv   = Dotenv.load();
    String url      = dotenv.get("DB_URL");
    String user     = dotenv.get("DB_USER");
    String password = dotenv.get("DB_PASSWORD");
    System.out.println("Connecting to: " + url + " .... ");

    try(Connection connection = DriverManager.getConnection(url, user, password)) {
      System.out.println("Database Connected Successfully");
    } catch (SQLException e) {
      e.printStackTrace();
    }
    // * For Smooth Program termination and Maven Shutdown
    com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
  }
}