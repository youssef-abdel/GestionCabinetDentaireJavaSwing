package com.dentalclinic;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;

import com.dentalclinic.ui.LoginDialog;
import com.dentalclinic.ui.MainFrame;

public class DentalClinicApplication {
    public static void main(String[] args) {
        try {
            // Set the modern look and feel
            UIManager.setLookAndFeel(new FlatLightLaf());
            
            // Create and display the login dialog
            SwingUtilities.invokeLater(() -> {
                LoginDialog loginDialog = new LoginDialog();
                loginDialog.setVisible(true);
                
                // After login is successful, show the main application window
                if (loginDialog.isLoginSuccessful()) {
                    MainFrame mainFrame = new MainFrame(loginDialog.getUserRole(), loginDialog.getUserId());
                    mainFrame.setVisible(true);
                } else {
                    System.exit(0);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
