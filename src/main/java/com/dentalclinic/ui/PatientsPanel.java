package com.dentalclinic.ui;

import javax.swing.*;
import java.awt.*;

public class PatientsPanel extends JPanel {
    public PatientsPanel() {
        setLayout(new BorderLayout());
        
        // Add a title label
        JLabel titleLabel = new JLabel("Patients Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);
        
        // Create a panel for the main content
        JPanel contentPanel = new JPanel(new BorderLayout());
        
        // Add a table to display patients
        String[] columnNames = {"ID", "Name", "Phone", "Email", "Last Visit"};
        Object[][] data = {}; // Empty data for now
        JTable patientsTable = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(patientsTable);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(new JButton("Add Patient"));
        buttonsPanel.add(new JButton("Edit Patient"));
        buttonsPanel.add(new JButton("Delete Patient"));
        
        contentPanel.add(buttonsPanel, BorderLayout.SOUTH);
        add(contentPanel, BorderLayout.CENTER);
    }
}
