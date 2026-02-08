# JDBC (MySQL and MAVEN)

## Prerequisites
  1. This Project Uses Maven as the Build System.
  2. Must Have JDK (8+ is Preferred).
  3. MySQL DBMS Must be Installed on the System.

## Build Instructions
  ### 1. Clone The Repository: 
  ```bash
  git clone https://github.com/X-RugvedCodes-X/JDBC-Learnings.git
  ```

  ### 2. Navigate to Any of the Java Project's Root Directory:
  ```bash
  cd JDBC-Learnings/jdbc-intro
  ```
  ### 3. Configure Your Environment Variables for Database Connection - Add Your url, username and password for Database Connection
  ```bash
  touch .env
  ```

  ### 4. From any Java Project's Root Directory Execute this Command:
  ```bash
  mvn compile exec:java
  ```
  And for Subsequent Modifications Use: `mvn exec:java`

