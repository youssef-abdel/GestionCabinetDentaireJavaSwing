package com.dentalclinic.ui;

import com.dentalclinic.util.DatabaseUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CalendarPanel extends JPanel {
    private int userId;
    private String userRole;
    private LocalDate currentDate;
    private JPanel calendarGrid;
    private JLabel monthLabel;
    private List<AppointmentInfo> appointments;
    private JPanel dayViewPanel;
    private final Color SELECTED_COLOR = new Color(91, 192, 222);
    private final Color TODAY_COLOR = new Color(223, 240, 216);
    private final Color APPOINTMENT_COLOR = new Color(66, 139, 202);
    private JPanel timeSlotPanel;
    private LocalDate selectedDate;

    public CalendarPanel(int userId, String userRole) {
        this.userId = userId;
        this.userRole = userRole;
        this.currentDate = LocalDate.now();
        this.selectedDate = currentDate;
        this.appointments = new ArrayList<>();
        initializeUI();
        loadAppointments();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top navigation panel
        JPanel navigationPanel = createNavigationPanel();
        add(navigationPanel, BorderLayout.NORTH);

        // Main content with calendar and day view
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.6);

        // Calendar panel
        JPanel calendarPanel = new JPanel(new BorderLayout(5, 5));
        calendarGrid = new JPanel(new GridLayout(0, 7, 2, 2));
        createCalendarGrid();
        calendarPanel.add(createWeekdayHeader(), BorderLayout.NORTH);
        calendarPanel.add(calendarGrid, BorderLayout.CENTER);

        // Day view panel
        dayViewPanel = createDayViewPanel();
        updateDayView(LocalDate.now());

        splitPane.setLeftComponent(calendarPanel);
        splitPane.setRightComponent(dayViewPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createNavigationPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        JButton prevMonth = new JButton("◀");
        monthLabel = new JLabel("", SwingConstants.CENTER);
        JButton nextMonth = new JButton("▶");
        JButton todayButton = new JButton("Aujourd'hui");

        // Style the buttons
        prevMonth.setFocusPainted(false);
        nextMonth.setFocusPainted(false);
        todayButton.setFocusPainted(false);

        prevMonth.addActionListener((_) -> changeMonth(-1));
        nextMonth.addActionListener((_) -> changeMonth(1));
        todayButton.addActionListener((_) -> goToToday());

        panel.add(prevMonth);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(monthLabel);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(nextMonth);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(todayButton);

        updateMonthLabel();
        return panel;
    }

    private JPanel createWeekdayHeader() {
        JPanel header = new JPanel(new GridLayout(1, 7));
        header.setBackground(new Color(245, 245, 245));
        String[] weekdays = {"Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam"};
        for (String day : weekdays) {
            JLabel label = new JLabel(day, SwingConstants.CENTER);
            label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            header.add(label);
        }
        return header;
    }

    private void createCalendarGrid() {
        calendarGrid.removeAll();
        System.out.println("Creating calendar grid for " + currentDate);
        
        YearMonth yearMonth = YearMonth.from(currentDate);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        LocalDate lastOfMonth = yearMonth.atEndOfMonth();

        // Fill in days before the first of the month
        LocalDate startDate = firstOfMonth.minusDays(firstOfMonth.getDayOfWeek().getValue() % 7);
        
        // Create all day panels first
        Map<LocalDate, DayPanel> dayPanels = new HashMap<>();
        for (LocalDate date = startDate; !date.isAfter(lastOfMonth); date = date.plusDays(1)) {
            DayPanel dayPanel = new DayPanel(date);
            if (date.getMonth() != currentDate.getMonth()) {
                dayPanel.setForeground(Color.GRAY);
            }
            if (date.equals(LocalDate.now())) {
                dayPanel.setBackground(TODAY_COLOR);
            }
            if (date.equals(selectedDate)) {
                dayPanel.setBackground(SELECTED_COLOR);
            }
            dayPanels.put(date, dayPanel);
            calendarGrid.add(dayPanel);
        }
        
        // Add appointments to the appropriate day panels
        for (AppointmentInfo apt : appointments) {
            LocalDate aptDate = apt.date.toLocalDate();
            System.out.println("Processing appointment for date: " + aptDate);
            
            DayPanel dayPanel = dayPanels.get(aptDate);
            if (dayPanel != null) {
                System.out.println("Adding appointment to panel: " + apt.patientName);
                dayPanel.addAppointment(apt);
            } else {
                System.out.println("No panel found for date: " + aptDate);
            }
        }

        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    private JPanel createDayViewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header for selected date
        JLabel dateLabel = new JLabel("", SwingConstants.CENTER);
        dateLabel.setFont(dateLabel.getFont().deriveFont(Font.BOLD, 14f));
        panel.add(dateLabel, BorderLayout.NORTH);

        // Time slots panel
        timeSlotPanel = new JPanel();
        timeSlotPanel.setLayout(new BoxLayout(timeSlotPanel, BoxLayout.Y_AXIS));
        
        JScrollPane scrollPane = new JScrollPane(timeSlotPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void updateDayView(LocalDate date) {
        selectedDate = date;
        timeSlotPanel.removeAll();
        
        // Update header
        JLabel dateLabel = (JLabel) dayViewPanel.getComponent(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy");
        dateLabel.setText(date.format(formatter));

        // Get appointments for selected date
        List<AppointmentInfo> dayAppointments = appointments.stream()
            .filter(apt -> apt.date.toLocalDate().equals(date))
            .sorted((a1, a2) -> a1.date.compareTo(a2.date))
            .collect(Collectors.toList());

        // Create time slots from 8:00 to 18:00
        LocalTime time = LocalTime.of(8, 0);
        while (!time.isAfter(LocalTime.of(18, 0))) {
            JPanel slotPanel = new JPanel(new BorderLayout());
            slotPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
            
            // Time label
            JLabel timeLabel = new JLabel(time.format(DateTimeFormatter.ofPattern("HH:mm")));
            timeLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            timeLabel.setPreferredSize(new Dimension(60, 25));
            slotPanel.add(timeLabel, BorderLayout.WEST);
            
            // Find appointments for this time slot
            final LocalTime slotTime = time;
            List<AppointmentInfo> slotAppointments = dayAppointments.stream()
                .filter(apt -> {
                    LocalTime aptTime = apt.date.toLocalTime();
                    LocalTime aptEndTime = aptTime.plusMinutes(apt.duration);
                    return !aptTime.isAfter(slotTime) && aptEndTime.isAfter(slotTime);
                })
                .collect(Collectors.toList());
                
            if (!slotAppointments.isEmpty()) {
                JPanel apptsPanel = new JPanel();
                apptsPanel.setLayout(new BoxLayout(apptsPanel, BoxLayout.Y_AXIS));
                
                for (AppointmentInfo appointment : slotAppointments) {
                    JPanel aptPanel = new JPanel(new BorderLayout());
                    aptPanel.setBackground(getAppointmentColor(appointment.status));
                    aptPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                    
                    String timeStr = appointment.date.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
                    JLabel timeEndLabel = new JLabel(timeStr + " (" + appointment.duration + "min)");
                    timeEndLabel.setForeground(Color.WHITE);
                    aptPanel.add(timeEndLabel, BorderLayout.WEST);
                    
                    JLabel patientLabel = new JLabel(appointment.patientName);
                    patientLabel.setForeground(Color.WHITE);
                    aptPanel.add(patientLabel, BorderLayout.CENTER);
                    
                    JLabel statusLabel = new JLabel(appointment.status);
                    statusLabel.setForeground(Color.WHITE);
                    aptPanel.add(statusLabel, BorderLayout.EAST);
                    
                    apptsPanel.add(aptPanel);
                    apptsPanel.add(Box.createVerticalStrut(2));
                }
                
                slotPanel.add(apptsPanel, BorderLayout.CENTER);
            }
            
            timeSlotPanel.add(slotPanel);
            time = time.plusMinutes(30);
        }
        
        timeSlotPanel.revalidate();
        timeSlotPanel.repaint();
        createCalendarGrid(); // Refresh calendar to show selected date
    }

    private void loadAppointments() {
        appointments.clear();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT a.*, p.first_name, p.last_name " +
                 "FROM appointments a " +
                 "JOIN patients p ON a.patient_id = p.id " +
                 (userRole.equalsIgnoreCase("doctor") ? "WHERE a.doctor_id = ?" : ""))) {
            
            if (userRole.equalsIgnoreCase("doctor")) {
                pstmt.setInt(1, userId);
            }
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                try {
                    Timestamp ts = rs.getTimestamp("appointment_date");
                    LocalDateTime appointmentDate = ts.toLocalDateTime();
                    String patientName = rs.getString("first_name") + " " + rs.getString("last_name");
                    
                    System.out.println("Loading appointment: Date=" + appointmentDate + 
                                     ", Patient=" + patientName + 
                                     ", Status=" + rs.getString("status"));
                    
                    appointments.add(new AppointmentInfo(
                        rs.getInt("id"),
                        appointmentDate,
                        rs.getInt("duration"),
                        rs.getString("status"),
                        patientName
                    ));
                } catch (Exception e) {
                    System.err.println("Error loading appointment: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("Total appointments loaded: " + appointments.size());
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des rendez-vous: " + e.getMessage(), 
                                        "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        
        createCalendarGrid();
        updateDayView(selectedDate);
    }

    public void refreshCalendar() {
        loadAppointments();
    }

    private void changeMonth(int delta) {
        currentDate = currentDate.plusMonths(delta);
        updateMonthLabel();
        loadAppointments();
    }

    private void goToToday() {
        currentDate = LocalDate.now();
        selectedDate = currentDate;
        updateMonthLabel();
        loadAppointments();
    }

    private void updateMonthLabel() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        monthLabel.setText(currentDate.format(formatter).substring(0, 1).toUpperCase() + 
                         currentDate.format(formatter).substring(1));
    }

    private Color getAppointmentColor(String status) {
        switch (status.toLowerCase()) {
            case "confirmed":
                return new Color(40, 167, 69); // Green
            case "pending":
                return new Color(255, 193, 7); // Yellow
            case "cancelled":
                return new Color(220, 53, 69); // Red
            default:
                return APPOINTMENT_COLOR;
        }
    }

    private class DayPanel extends JPanel {
        private LocalDate date;
        private List<AppointmentInfo> dayAppointments;
        private JPanel appointmentsPanel;

        public DayPanel(LocalDate initialDate) {
            this.date = initialDate;
            this.dayAppointments = new ArrayList<>();
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            setBackground(Color.WHITE);

            // Day number label
            JLabel dayLabel = new JLabel(String.valueOf(initialDate.getDayOfMonth()), SwingConstants.CENTER);
            dayLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            add(dayLabel, BorderLayout.NORTH);

            // Panel for appointment indicators
            appointmentsPanel = new JPanel();
            appointmentsPanel.setLayout(new BoxLayout(appointmentsPanel, BoxLayout.Y_AXIS));
            appointmentsPanel.setBackground(getBackground());
            appointmentsPanel.setPreferredSize(new Dimension(50, 30)); // Set minimum height
            add(appointmentsPanel, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    updateDayView(date);
                }
            });
        }

        public void addAppointment(AppointmentInfo apt) {
            if (!dayAppointments.contains(apt)) {
                dayAppointments.add(apt);
                
                JPanel indicator = new JPanel();
                indicator.setBackground(getAppointmentColor(apt.status));
                indicator.setPreferredSize(new Dimension(getWidth(), 6));
                indicator.setMinimumSize(new Dimension(10, 6));
                indicator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
                
                // Add tooltip with appointment info
                String tooltipText = String.format("<html>%s<br>%s<br>%s</html>",
                    apt.patientName,
                    apt.date.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                    apt.status
                );
                indicator.setToolTipText(tooltipText);
                
                appointmentsPanel.add(Box.createVerticalStrut(2));
                appointmentsPanel.add(indicator);
                
                System.out.println("Added appointment indicator for: " + apt.patientName + " on " + date);
                
                appointmentsPanel.revalidate();
                appointmentsPanel.repaint();
                revalidate();
                repaint();
            }
        }
    }

    private static class AppointmentInfo {
       
        LocalDateTime date;
        int duration;
        String status;
        String patientName;

        public AppointmentInfo(int id, LocalDateTime date, int duration, String status, String patientName) {
            
            this.date = date;
            this.duration = duration;
            this.status = status;
            this.patientName = patientName;
        }
    }
}
