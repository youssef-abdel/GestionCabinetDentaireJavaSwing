package com.dentalclinic.ui;

import com.dentalclinic.util.DatabaseUtil;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;
import com.toedter.calendar.JDateChooser;

public class PatientRecordsPanel extends JPanel {
    private String userRole;
    private JTable patientsTable;
    private DefaultTableModel tableModel;
    private JPanel detailsPanel;
    private JTextField searchField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JDateChooser birthDateChooser;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextArea addressArea;
    private JTextArea medicalHistoryArea;
    private int selectedPatientId = -1;

    public PatientRecordsPanel(int userId, String userRole) {
        this.userRole = userRole;
        initializeUI();
        loadPatients();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Panel with Search
        JPanel topPanel = new JPanel(new BorderLayout());
        searchField = new JTextField(20);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { searchPatients(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { searchPatients(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { searchPatients(); }
        });
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Rechercher:"));
        searchPanel.add(searchField);
        
        JButton addButton = new JButton("Nouveau Patient");
        addButton.addActionListener((_) -> showAddPatientDialog());
        // Only show add button for admin and doctor roles
        addButton.setVisible("ADMIN".equals(userRole) || "DOCTOR".equals(userRole));
        
        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(addButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.4);

        // Patients Table
        createPatientsTable();
        JScrollPane tableScrollPane = new JScrollPane(patientsTable);
        splitPane.setLeftComponent(tableScrollPane);

        // Details Panel
        detailsPanel = createDetailsPanel();
        splitPane.setRightComponent(new JScrollPane(detailsPanel));

        add(splitPane, BorderLayout.CENTER);
    }

    private void createPatientsTable() {
        String[] columns = {"ID", "Nom", "Prénom", "Téléphone", "Email"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        patientsTable = new JTable(tableModel);
        patientsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = patientsTable.getSelectedRow();
                if (row != -1) {
                    selectedPatientId = (int) patientsTable.getValueAt(row, 0);
                    loadPatientDetails(selectedPatientId);
                }
            }
        });
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Patient Information Section
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Informations du Patient"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // First Name
        gbc.gridx = 0; gbc.gridy = 0;
        infoPanel.add(new JLabel("Prénom:"), gbc);
        gbc.gridx = 1;
        firstNameField = new JTextField(20);
        infoPanel.add(firstNameField, gbc);

        // Last Name
        gbc.gridx = 0; gbc.gridy = 1;
        infoPanel.add(new JLabel("Nom:"), gbc);
        gbc.gridx = 1;
        lastNameField = new JTextField(20);
        infoPanel.add(lastNameField, gbc);

        // Birth Date
        gbc.gridx = 0; gbc.gridy = 2;
        infoPanel.add(new JLabel("Date de naissance:"), gbc);
        gbc.gridx = 1;
        birthDateChooser = new JDateChooser();
        infoPanel.add(birthDateChooser, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 3;
        infoPanel.add(new JLabel("Téléphone:"), gbc);
        gbc.gridx = 1;
        phoneField = new JTextField(20);
        infoPanel.add(phoneField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 4;
        infoPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        emailField = new JTextField(20);
        infoPanel.add(emailField, gbc);

        // Address
        gbc.gridx = 0; gbc.gridy = 5;
        infoPanel.add(new JLabel("Adresse:"), gbc);
        gbc.gridx = 1;
        addressArea = new JTextArea(3, 20);
        addressArea.setLineWrap(true);
        infoPanel.add(new JScrollPane(addressArea), gbc);

        // Medical History
        gbc.gridx = 0; gbc.gridy = 6;
        infoPanel.add(new JLabel("Antécédents:"), gbc);
        gbc.gridx = 1;
        medicalHistoryArea = new JTextArea(5, 20);
        medicalHistoryArea.setLineWrap(true);
        infoPanel.add(new JScrollPane(medicalHistoryArea), gbc);

        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Enregistrer");
        saveButton.addActionListener((_) -> savePatientDetails());
        buttonsPanel.add(saveButton);

        panel.add(infoPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(buttonsPanel);

        return panel;
    }

    private void loadPatients() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT id, first_name, last_name, phone, email " +
                 "FROM patients ORDER BY last_name, first_name")) {
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("last_name"));
                row.add(rs.getString("first_name"));
                row.add(rs.getString("phone"));
                row.add(rs.getString("email"));
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des patients",
                                        "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPatientDetails(int patientId) {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT * FROM patients WHERE id = ?")) {
            
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                firstNameField.setText(rs.getString("first_name"));
                lastNameField.setText(rs.getString("last_name"));
                if (rs.getDate("date_of_birth") != null) {
                    birthDateChooser.setDate(rs.getDate("date_of_birth"));
                }
                phoneField.setText(rs.getString("phone"));
                emailField.setText(rs.getString("email"));
                addressArea.setText(rs.getString("address"));
                medicalHistoryArea.setText(rs.getString("medical_history"));

                // Set fields editable based on user role
                boolean canEdit = "ADMIN".equals(userRole) || "DOCTOR".equals(userRole);
                firstNameField.setEditable(canEdit);
                lastNameField.setEditable(canEdit);
                birthDateChooser.setEnabled(canEdit);
                phoneField.setEditable(canEdit);
                emailField.setEditable(canEdit);
                addressArea.setEditable(canEdit);
                medicalHistoryArea.setEditable(canEdit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des détails du patient",
                                        "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void savePatientDetails() {
        if (selectedPatientId == -1) return;

        // Check if user has permission to save
        if (!("ADMIN".equals(userRole) || "DOCTOR".equals(userRole))) {
            JOptionPane.showMessageDialog(this, 
                "Vous n'avez pas les permissions nécessaires pour modifier les informations du patient.",
                "Accès Refusé", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate required fields
        if (firstNameField.getText().trim().isEmpty() || lastNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Le nom et le prénom sont obligatoires.",
                "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "UPDATE patients SET " +
                 "first_name = ?, " +
                 "last_name = ?, " +
                 "date_of_birth = ?, " +
                 "phone = ?, " +
                 "email = ?, " +
                 "address = ?, " +
                 "medical_history = ? " +
                 "WHERE id = ?")) {
            
            pstmt.setString(1, firstNameField.getText().trim());
            pstmt.setString(2, lastNameField.getText().trim());
            pstmt.setDate(3, birthDateChooser.getDate() != null ? 
                         new java.sql.Date(birthDateChooser.getDate().getTime()) : null);
            pstmt.setString(4, phoneField.getText().trim().isEmpty() ? null : phoneField.getText().trim());
            pstmt.setString(5, emailField.getText().trim().isEmpty() ? null : emailField.getText().trim());
            pstmt.setString(6, addressArea.getText().trim().isEmpty() ? null : addressArea.getText().trim());
            pstmt.setString(7, medicalHistoryArea.getText().trim().isEmpty() ? null : medicalHistoryArea.getText().trim());
            pstmt.setInt(8, selectedPatientId);
            
            int updated = pstmt.executeUpdate();
            if (updated > 0) {
                JOptionPane.showMessageDialog(this, "Patient mis à jour avec succès",
                                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                loadPatients(); // Refresh the table
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Aucune mise à jour effectuée. Le patient n'existe peut-être plus.",
                    "Avertissement", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Erreur lors de la mise à jour du patient: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchPatients() {
        String searchTerm = searchField.getText().toLowerCase();
        tableModel.setRowCount(0);
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT id, first_name, last_name, phone, email FROM patients " +
                 "WHERE LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ? OR phone LIKE ? " +
                 "ORDER BY last_name, first_name")) {
            
            String term = "%" + searchTerm + "%";
            pstmt.setString(1, term);
            pstmt.setString(2, term);
            pstmt.setString(3, term);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("last_name"));
                row.add(rs.getString("first_name"));
                row.add(rs.getString("phone"));
                row.add(rs.getString("email"));
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAddPatientDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nouveau Patient", true);
        dialog.setLayout(new BorderLayout(10, 10));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Add form fields
        JTextField newFirstName = new JTextField(20);
        JTextField newLastName = new JTextField(20);
        JTextField newPhone = new JTextField(20);
        JTextField newEmail = new JTextField(20);
        
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Prénom:"), gbc);
        gbc.gridx = 1;
        formPanel.add(newFirstName, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Nom:"), gbc);
        gbc.gridx = 1;
        formPanel.add(newLastName, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Téléphone:"), gbc);
        gbc.gridx = 1;
        formPanel.add(newPhone, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        formPanel.add(newEmail, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Enregistrer");
        JButton cancelButton = new JButton("Annuler");
        
        saveButton.addActionListener((_) -> {
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO patients (first_name, last_name, phone, email) VALUES (?, ?, ?, ?)")) {
                
                pstmt.setString(1, newFirstName.getText());
                pstmt.setString(2, newLastName.getText());
                pstmt.setString(3, newPhone.getText());
                pstmt.setString(4, newEmail.getText());
                
                pstmt.executeUpdate();
                
                dialog.dispose();
                loadPatients(); // Refresh the table
                JOptionPane.showMessageDialog(this, "Patient ajouté avec succès",
                                            "Succès", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erreur lors de l'ajout du patient",
                                            "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener((_) -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
