Campus Lost and Found Portal.

A desktop-based Campus Lost and Found Portal developed using JavaFX, Java Servlets, JDBC and MySQL. The application helps students report lost or found belongings, search for items, submit claims and track their reports. An administrator can manage reported items and process claims.

Features

Student Features

Student Registration – Create an account using student details.

Student Login – Login using USN or email.

Report Lost Items – Submit details about lost belongings.

Report Found Items – Report items found on campus.

Search Items – Search and filter reported items.

Claim Items – Submit a claim description for a found item.

My Reports – View items reported by the logged-in student.

My Claims – View claims submitted by the logged-in student.

Secure Password Storage – Passwords are hashed using PBKDF2.

Admin Features

Admin login.

View reported lost and found items.

View submitted claims.

Approve or reject claims.

Update item statuses.

Delete reported items.

Refresh item and claim lists.

Technologies Used

Technology

Purpose

Java

Application programming language

JavaFX

Desktop graphical user interface

Java Servlets

Backend request processing

JDBC

Database connectivity

MySQL

Database management

Apache Tomcat 10.1

Servlet server

IntelliJ IDEA Community Edition

Development environment

Git & GitHub

Version control

System Architecture

The project follows a frontend-backend-database architecture.

JavaFX Desktop Application
        |
        | HTTP Requests (JSON / Form Data)
        v
Java Servlets (Apache Tomcat)
        |
        | JDBC
        v
MySQL Database

Architecture Components

Frontend: JavaFX handles the graphical interface and user interactions.

API Service: Java HTTP Client communicates with backend servlets.

Backend: Java Servlets handle authentication, reports, claims and administrative operations.

Database: MySQL stores students, items, claims and admin data.

Server: Apache Tomcat hosts the backend servlet application.

Project Structure

CampusLostAndFoundPortal/
│
├── src/
│   ├── Main.java
│   ├── frontend/
│   │   ├── LoginView.java
│   │   ├── RegisterView.java
│   │   ├── Navigation.java
│   │   ├── DashboardView.java
│   │   ├── ReportLostView.java
│   │   ├── ReportFoundView.java
│   │   ├── SearchView.java
│   │   ├── MyReportsView.java
│   │   ├── MyClaimsView.java
│   │   ├── AdminLoginView.java
│   │   └── AdminDashboardView.java
│   │
│   └── service/
│       └── ApiService.java
│
├── Backend/
│   ├── src/
│   │   ├── Main.java
│   │   └── backend/
│   │       ├── servlet/
│   │       │   ├── RegisterServlet.java
│   │       │   ├── LoginServlet.java
│   │       │   ├── ItemServlet.java
│   │       │   ├── SearchServlet.java
│   │       │   ├── MyReportsServlet.java
│   │       │   ├── ClaimServlet.java
│   │       │   └── AdminServlet.java
│   │       │
│   │       └── util/
│   │           └── DBConnection.java
│   │
├── .gitignore
├── CampusLostAndFound.iml
└── Backend/

Database

The application uses MySQL database:

campus_lost_found

Database Tables

Table

Description

students

Stores registered student details

items

Stores lost and found item reports

claims

Stores claim requests made by students

admins

Stores administrator credentials

Item Statuses

OPEN – Item is available for action.

CLAIMED – Item has been claimed.

CLOSED – Item report has been closed.

Claim Statuses

PENDING – Awaiting administrator review.

APPROVED – Claim has been approved.

REJECTED – Claim has been rejected.

Installation and Setup

Prerequisites

Install the following:

Java JDK

JavaFX SDK

IntelliJ IDEA Community Edition

MySQL Server

MySQL Workbench

Apache Tomcat 10.1

MySQL Connector/J

1. Clone the Repository

git clone https://github.com/Ayush-1242/CampusLostAndFoundPortal.git

Open the project in IntelliJ IDEA.

2. Configure JavaFX

Add the JavaFX SDK libraries to the project in IntelliJ IDEA.

Configure the VM options for the JavaFX application:

--module-path "PATH_TO_JAVAFX_LIB" --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics

Replace PATH_TO_JAVAFX_LIB with the path to your JavaFX SDK lib folder.

3. Configure MySQL

Create the database:

CREATE DATABASE campus_lost_found;

Create the required tables (students, items, claims and admins) according to the database schema used by the application.

4. Configure Database Credentials

The backend reads the database password from the DB_PASSWORD environment variable.

Configure this variable on your operating system with your MySQL password.

The database connection is configured in:

Backend/src/backend/util/DBConnection.java

The connection uses:

Database: campus_lost_found

Host: localhost

Port: 3306

Username: root

The password is not hardcoded in the source code.

5. Configure Apache Tomcat

Install Apache Tomcat 10.1.

Compile the Backend module in IntelliJ IDEA and deploy the compiled backend classes into Tomcat's application directory.

The servlet application is deployed under:

webapps/CampusLostAndFound/WEB-INF/classes/

Ensure that the Jakarta Servlet API and MySQL Connector/J are available to the backend.

Start Apache Tomcat.

6. Run the Application

Run:

src/Main.java

The JavaFX desktop application will open.

The frontend communicates with the backend hosted on:

http://localhost:8080/CampusLostAndFound

Ensure MySQL and Apache Tomcat are running before using the application.

Application Workflow

A student registers and logs in.

The student reports a lost or found item.

Students search for reported items.

A student submits a claim for an eligible found item.

The claim is stored in the MySQL database.

The administrator reviews the claim.

The administrator approves or rejects the claim.

The item status is updated accordingly.

Security and Validation

Password hashing using PBKDF2.

Session-based student and admin authentication.

Prevents students from claiming their own reported items.

Prevents duplicate pending claims.

Validates item and claim operations.

Database credentials are stored in an environment variable rather than hardcoded.

Project Objective

The objective of this project is to provide a centralized digital platform for managing lost and found belongings within a college campus. It reduces the difficulty of locating lost items and facilitates communication between students and administrators through a structured reporting and claim management system.

Author

Developed as a college Java project.

License

This project is intended for educational purposes.
