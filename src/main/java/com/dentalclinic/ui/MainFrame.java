package com.dentalclinic.ui;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;

/**
 * Main application frame.
 */
public class MainFrame extends JFrame {
    private String userRole;
    private int userId;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JPanel sidebarPanel;
    private JPanel contentPanel;

    // UI Components
    private PatientRecordsPanel patientRecordsPanel;
    private AppointmentPanel appointmentPanel;
    private CalendarPanel calendarPanel;

    public MainFrame(String userRole, int userId) {
        this.userRole = userRole;
        this.userId = userId;
        setupLookAndFeel();
        initializeUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }

    private void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeUI() {
        setTitle("Cabinet Dentaire - " + userRole);

        mainPanel = new JPanel(new BorderLayout());
        
        // Create sidebar
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(new Color(51, 51, 51));
        sidebarPanel.setPreferredSize(new Dimension(200, getHeight()));
        
        // Add navigation buttons based on user role
        if ("SECRETARY".equals(userRole.toUpperCase())) {
            // Secretary features
            addSidebarButton("Rendez-vous", "APPOINTMENTS");
            addSidebarButton("Patients", "PATIENTS");
            addSidebarButton("Calendrier", "CALENDAR");
        } else if ("ADMIN".equals(userRole.toUpperCase())) {
            // Dentist/Admin features
            addSidebarButton("Dossiers Médicaux", "PATIENTS");
            addSidebarButton("Rendez-vous", "APPOINTMENTS");
            addSidebarButton("Calendrier", "CALENDAR");
        } else if ("PRACTITIONER".equals(userRole.toUpperCase())) {
            // Dentist features
            addSidebarButton("Dossiers Médicaux", "PATIENTS");
            addSidebarButton("Rendez-vous", "APPOINTMENTS");
            addSidebarButton("Calendrier", "CALENDAR");
        }
        
        // Add logout button at the bottom of sidebar
        JButton logoutButton = new JButton("Déconnexion");
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton.setMaximumSize(new Dimension(180, 40));
        logoutButton.setBackground(new Color(220, 53, 69)); // Red color
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener((_) -> logout());
        
        // Add a glue component to push the logout button to the bottom
        sidebarPanel.add(Box.createVerticalGlue());
        sidebarPanel.add(Box.createVerticalStrut(10)); // Add some padding
        sidebarPanel.add(logoutButton);
        sidebarPanel.add(Box.createVerticalStrut(10)); // Add some padding at the bottom

        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        
        // Create content panel
        contentPanel = new JPanel(new CardLayout());
        cardLayout = (CardLayout) contentPanel.getLayout();

        // Initialize panels
        patientRecordsPanel = new PatientRecordsPanel(userId, userRole);
        appointmentPanel = new AppointmentPanel(userId, userRole);
        calendarPanel = new CalendarPanel(userId, userRole);

        // Add panels to content
        contentPanel.add(patientRecordsPanel, "PATIENTS");
        contentPanel.add(appointmentPanel, "APPOINTMENTS");
        contentPanel.add(calendarPanel, "CALENDAR");

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);
        setVisible(true);
    }

    private void addSidebarButton(String text, String cardName) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, 40));
        button.setBackground(new Color(51, 51, 51));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(75, 75, 75));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(51, 51, 51));
            }
        });
        
        button.addActionListener((_) -> cardLayout.show(contentPanel, cardName));
        sidebarPanel.add(button);
        sidebarPanel.add(Box.createVerticalStrut(5));
    }

    private void logout() {
        dispose(); // Close the current window
        SwingUtilities.invokeLater(() -> {
            // Create and show a new login dialog
            LoginDialog loginDialog = new LoginDialog();
            loginDialog.setVisible(true);
            
            // If login is successful, create a new main frame
            if (loginDialog.isLoginSuccessful()) {
                MainFrame mainFrame = new MainFrame(loginDialog.getUserRole(), loginDialog.getUserId());
                mainFrame.setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }

    public void refreshCalendar() {
        if (calendarPanel != null) {
            calendarPanel.refreshCalendar();
        }
    }

    public CalendarPanel getCalendarPanel() {
        return calendarPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame("ADMIN", 1));
    }
}
