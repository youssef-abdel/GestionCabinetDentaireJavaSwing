package com.dentalclinic.ui;

import com.dentalclinic.util.DatabaseUtil;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.*;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

public class AppointmentPanel extends JPanel {
    private final int userId;
    private final String userRole;
    private JTable appointmentsTable;
    private DefaultTableModel tableModel;
    private JDateChooser dateChooser;
    private JSpinner timeSpinner;
    private JComboBox<String> patientComboBox;
    private JComboBox<String> doctorComboBox;
    private JTextArea notesArea;

    public AppointmentPanel(int userId, String userRole) {
        this.userId = userId;
        this.userRole = userRole;
        initializeUI();
        loadAppointments();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create top panel for controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Only show add/edit buttons for secretary
        if ("SECRETARY".equals(userRole.toUpperCase())) {
            JButton addButton = new JButton("Nouveau Rendez-vous");
            addButton.addActionListener((_)-> showAppointmentDialog());
            controlPanel.add(addButton);

            JButton deleteButton = new JButton("Annuler RDV");
            deleteButton.addActionListener((_)-> deleteAppointment());
            controlPanel.add(deleteButton);
        }

        add(controlPanel, BorderLayout.NORTH);

        // Create table
        String[] columnNames = {"ID", "Patient", "Docteur", "Date", "Heure", "Status", "Notes"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        appointmentsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(appointmentsTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadPatients() {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, first_name, last_name FROM patients ORDER BY last_name, first_name")) {
            
            patientComboBox.removeAllItems();
            while (rs.next()) {
                String patientInfo = rs.getInt("id") + " - " + rs.getString("first_name") + " " + rs.getString("last_name");
                patientComboBox.addItem(patientInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des patients", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDoctors() {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, username FROM users WHERE role = 'ADMIN' ORDER BY username")) {
            
            doctorComboBox.removeAllItems();
            while (rs.next()) {
                String doctorInfo = rs.getInt("id") + " - " + rs.getString("username");
                doctorComboBox.addItem(doctorInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des docteurs", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAppointments() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT a.id, p.first_name || ' ' || p.last_name as patient_name, " +
                 "u.username as doctor_name, a.appointment_date, a.status, a.notes " +
                 "FROM appointments a " +
                 "JOIN patients p ON a.patient_id = p.id " +
                 "JOIN users u ON a.practitioner_id = u.id " +
                 (userRole.equalsIgnoreCase("ADMIN") ? "WHERE a.practitioner_id = ? " : "") +
                 "ORDER BY a.appointment_date DESC")) {
            
            if (userRole.equalsIgnoreCase("ADMIN")) {
                stmt.setInt(1, userId);
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalDateTime dateTime = rs.getTimestamp("appointment_date").toLocalDateTime();
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("patient_name"));
                row.add(rs.getString("doctor_name"));
                row.add(dateTime.toLocalDate().toString());
                row.add(dateTime.toLocalTime().toString());
                row.add(rs.getString("status"));
                row.add(rs.getString("notes"));
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des rendez-vous", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAppointmentDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nouveau Rendez-vous", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;

        // Patient selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Patient:"), gbc);

        gbc.gridx = 1;
        patientComboBox = new JComboBox<>();
        loadPatients();
        formPanel.add(patientComboBox, gbc);

        // Doctor selection
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Docteur:"), gbc);

        gbc.gridx = 1;
        doctorComboBox = new JComboBox<>();
        loadDoctors();
        formPanel.add(doctorComboBox, gbc);

        // Date selection
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Date:"), gbc);

        gbc.gridx = 1;
        dateChooser = new JDateChooser();
        dateChooser.setDate(java.util.Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        formPanel.add(dateChooser, gbc);

        // Time selection
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Heure:"), gbc);

        gbc.gridx = 1;
        SpinnerDateModel timeModel = new SpinnerDateModel();
        timeSpinner = new JSpinner(timeModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        formPanel.add(timeSpinner, gbc);

        // Notes
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Notes:"), gbc);

        gbc.gridx = 1;
        notesArea = new JTextArea(4, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        formPanel.add(notesScroll, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Enregistrer");
        JButton cancelButton = new JButton("Annuler");

        saveButton.addActionListener((_)-> {
            if (addAppointment()) {
                dialog.dispose();
                loadAppointments();
            }
        });

        cancelButton.addActionListener((_)-> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private boolean addAppointment() {
        try {
            if (patientComboBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un patient", "Erreur", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (doctorComboBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un docteur", "Erreur", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (dateChooser.getDate() == null) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner une date", "Erreur", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Get patient ID from combo box selection
            String patientSelection = (String) patientComboBox.getSelectedItem();
            int patientId = Integer.parseInt(patientSelection.split(" - ")[0]);

            // Get doctor ID from combo box selection
            String doctorSelection = (String) doctorComboBox.getSelectedItem();
            int doctorId = Integer.parseInt(doctorSelection.split(" - ")[0]);

            // Combine date and time
            LocalDate date = dateChooser.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalTime time = ((java.util.Date) timeSpinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
            LocalDateTime appointmentDateTime = LocalDateTime.of(date, time);

            // Insert into database
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO appointments (patient_id, practitioner_id, appointment_date, notes, status) VALUES (?, ?, ?, ?, ?)")) {
                
                pstmt.setInt(1, patientId);
                pstmt.setInt(2, doctorId);
                pstmt.setTimestamp(3, Timestamp.valueOf(appointmentDateTime));
                pstmt.setString(4, notesArea.getText());
                pstmt.setString(5, "SCHEDULED");
                
                pstmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Rendez-vous ajouté avec succès", "Succès", JOptionPane.INFORMATION_MESSAGE);
                
                // Refresh calendar
                MainFrame mainFrame = (MainFrame) SwingUtilities.getWindowAncestor(this);
                if (mainFrame != null) {
                    mainFrame.refreshCalendar();
                }
                
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Erreur lors de l'ajout du rendez-vous: " + e.getMessage(), 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void deleteAppointment() {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner un rendez-vous à annuler",
                "Aucune sélection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int appointmentId = (int) appointmentsTable.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Êtes-vous sûr de vouloir annuler ce rendez-vous ?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "UPDATE appointments SET status = 'CANCELLED' WHERE id = ?")) {
                
                pstmt.setInt(1, appointmentId);
                pstmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this,
                    "Rendez-vous annulé avec succès",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
                    
                loadAppointments();
                
                // Refresh calendar
                MainFrame mainFrame = (MainFrame) SwingUtilities.getWindowAncestor(this);
                if (mainFrame != null) {
                    mainFrame.refreshCalendar();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'annulation du rendez-vous: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
