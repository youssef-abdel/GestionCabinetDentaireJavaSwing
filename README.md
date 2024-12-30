# Dental Clinic Management System

A comprehensive Java-based desktop application for managing dental clinic operations, appointments, and patient records.

## Features

- **Patient Management**: Store and manage patient records and medical histories
- **Appointment Scheduling**: Calendar-based appointment scheduling system
- **User Authentication**: Secure login system for staff members
- **Modern UI**: Clean and intuitive user interface using FlatLaf Look and Feel
- **Email Notifications**: Automated email notifications for appointments

## Technologies Used

- Java 23
- SQLite Database
- Maven for dependency management
- JavaMail API for email notifications
- JCalendar for appointment scheduling
- FlatLaf for modern UI styling

## Prerequisites

- Java Development Kit (JDK) 23 or higher
- Maven 3.x
- SQLite Database

## Installation

1. Clone the repository:
git clone https://github.com/youssef-abdel/GestionCabinetDentaireJavaSwing.git

2-Navigate to the project directory:
cd dental-clinic-management

3-Build the project using Maven:
mvn clean install

4-Run the application:
java -jar target/dental-clinic-management-1.0-SNAPSHOT.jar

Project Structure:
src/main/java/com/dentalclinic/ui/ - User interface components
src/main/java/com/dentalclinic/model/ - Data models
src/main/java/com/dentalclinic/dao/ - Database access objects
src/main/java/com/dentalclinic/util/ - Utility classes

Dependencies:
SQLite JDBC Driver (3.43.0.0)
JavaMail API (1.6.2)
JCalendar (1.4)
FlatLaf (3.2.1)

Contributing:
1-Fork the repository
2-Create your feature branch
3-Commit your changes
4-Push to the branch
5-Create a new Pull Request

License
This project is licensed under the MIT License - see the LICENSE file for details
