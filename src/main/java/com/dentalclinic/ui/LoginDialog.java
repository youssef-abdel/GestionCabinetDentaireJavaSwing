package com.dentalclinic.ui;

import com.dentalclinic.util.DatabaseUtil;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private boolean loginSuccessful = false;
    private String userRole;
    private int userId;

    public LoginDialog() {
        super((Frame)null, "Connexion", true);
        initializeUI();
    }

    public LoginDialog(Frame parent) {
        super(parent, "Connexion", true);
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Create main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username field
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Nom d'utilisateur:"), gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(15);
        mainPanel.add(usernameField, gbc);

        // Password field
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Mot de passe:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        mainPanel.add(passwordField, gbc);

        // Login button
        JButton loginButton = new JButton("Connexion");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(loginButton, gbc);

        loginButton.addActionListener((_) -> handleLogin());

        // Add main panel to dialog
        add(mainPanel, BorderLayout.CENTER);

        // Handle Enter key
        getRootPane().setDefaultButton(loginButton);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try (Connection conn = DatabaseUtil.getConnection()) {
            String query = "SELECT id, role FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password); // In production, use proper password hashing

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        loginSuccessful = true;
                        userId = rs.getInt("id");
                        userRole = rs.getString("role");
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Nom d'utilisateur ou mot de passe incorrect",
                            "Erreur de connexion",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Erreur de connexion à la base de données",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }

    public String getUserRole() {
        return userRole;
    }

    public int getUserId() {
        return userId;
    }
}
