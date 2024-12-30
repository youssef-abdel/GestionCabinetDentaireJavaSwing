# **Dental Clinic Management System**  
A robust Java-based desktop application designed to streamline dental clinic operations, including managing appointments, patient records, and staff authentication.

---

## **Key Features**  

### **Patient Management**  
- Store, update, and retrieve patient records, including detailed medical histories.  

### **Appointment Scheduling**  
- Intuitive calendar-based system for scheduling and managing appointments.  

### **Secure User Authentication**  
- Multi-level secure login system for clinic staff, ensuring data confidentiality.  

### **Modern User Interface**  
- A clean, user-friendly interface designed with **FlatLaf Look and Feel** for a contemporary appearance.  

### **Email Notifications**  
- Automated email reminders and notifications for appointment confirmations and changes.  

---

## **Technologies Utilized**  

- **Programming Language**: Java 23  
- **Database**: SQLite  
- **Build Tool**: Maven  
- **Email Integration**: JavaMail API  
- **Scheduling**: JCalendar  
- **UI Styling**: FlatLaf  

---

## **System Requirements**  

To run this application, ensure the following prerequisites are met:  

- **Java Development Kit (JDK)**: Version 23 or higher  
- **Maven**: Version 3.x or later  
- **SQLite Database**  

---

## **Installation Guide**  

Follow these steps to set up and run the Dental Clinic Management System:  

1. **Clone the Repository**  
   ```bash  
   git clone https://github.com/youssef-abdel/GestionCabinetDentaireJavaSwing.git  
   ```  

2. **Navigate to the Project Directory**  
   ```bash  
   cd dental-clinic-management  
   ```  

3. **Build the Project Using Maven**  
   ```bash  
   mvn clean install  
   ```  

4. **Run the Application**  
   ```bash  
   java -jar target/dental-clinic-management-1.0-SNAPSHOT.jar  
   ```  

---

## **Project Directory Structure**  

The project is organized into the following modules:  

- **`src/main/java/com/dentalclinic/ui/`**: User Interface components  
- **`src/main/java/com/dentalclinic/model/`**: Data models  
- **`src/main/java/com/dentalclinic/dao/`**: Database Access Objects  
- **`src/main/java/com/dentalclinic/util/`**: Utility classes  

---

## **Dependencies**  

The application relies on the following libraries and tools:  

- **SQLite JDBC Driver**: Version 3.43.0.0  
- **JavaMail API**: Version 1.6.2  
- **JCalendar**: Version 1.4  
- **FlatLaf**: Version 3.2.1  

---

## **Contributing Guidelines**  

Contributions are welcome! To contribute:  

1. Fork the repository.  
2. Create a new feature branch:  
   ```bash  
   git checkout -b feature/your-feature-name  
   ```  
3. Commit your changes:  
   ```bash  
   git commit -m "Add your message here"  
   ```  
4. Push to your branch:  
   ```bash  
   git push origin feature/your-feature-name  
   ```  
5. Open a pull request.  

---

## **License**  

This project is licensed under the **MIT License**. See the `LICENSE` file for full details.  

