package com.bank.ui;

import com.bank.model.Customer;
import com.bank.service.BankingService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RegisterPanel extends JPanel {
    private final MainFrame mainFrame;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JPasswordField passwordField;

    public RegisterPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout()); // Centers the card
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Create the card panel
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 63, 65), 1, true),
                new EmptyBorder(30, 40, 30, 40)
        ));
        cardPanel.setPreferredSize(new Dimension(420, 530));
        cardPanel.setMaximumSize(new Dimension(420, 530));

        // Header Title
        JLabel titleLabel = new JLabel("YENİ HESAP AÇ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(52, 152, 219));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Saniyeler içinde bankacılık hesabınızı oluşturun");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        cardPanel.add(titleLabel);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        cardPanel.add(subtitleLabel);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        // Full Name Field
        nameField = createFormField(cardPanel, "Ad Soyad");
        // Email Field
        emailField = createFormField(cardPanel, "E-posta");
        // Phone Field
        phoneField = createFormField(cardPanel, "Telefon Numarası");

        // Password Field
        JLabel passwordLabel = new JLabel("Şifre");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setMaximumSize(new Dimension(340, 36));
        passwordField.setPreferredSize(new Dimension(340, 36));
        passwordField.putClientProperty("JTextField.showClearButton", true);
        cardPanel.add(passwordLabel);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        cardPanel.add(passwordField);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        // Register Button
        JButton registerButton = new JButton("Kayıt Ol");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerButton.setForeground(Color.WHITE);
        registerButton.setBackground(new Color(46, 204, 113)); // Green Primary Button
        registerButton.setMaximumSize(new Dimension(340, 40));
        registerButton.setPreferredSize(new Dimension(340, 40));
        registerButton.setFocusPainted(false);
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.addActionListener(e -> handleRegister());

        // Back to Login Link
        JButton backButton = new JButton("Zaten hesabınız var mı? Giriş Yapın");
        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        backButton.setForeground(Color.GRAY);
        backButton.setContentAreaFilled(false);
        backButton.setBorderPainted(false);
        backButton.setFocusPainted(false);
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> {
            clearFields();
            mainFrame.showLogin();
        });

        cardPanel.add(registerButton);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        cardPanel.add(backButton);

        add(cardPanel);
    }

    private JTextField createFormField(JPanel parent, String labelText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField textField = new JTextField();
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setMaximumSize(new Dimension(340, 36));
        textField.setPreferredSize(new Dimension(340, 36));
        textField.putClientProperty("JTextField.showClearButton", true);

        parent.add(label);
        parent.add(Box.createRigidArea(new Dimension(0, 5)));
        parent.add(textField);
        parent.add(Box.createRigidArea(new Dimension(0, 15)));

        return textField;
    }

    private void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        try {
            BankingService service = mainFrame.getBankingService();
            Customer customer = service.registerCustomer(name, email, phone, password);

            JOptionPane.showMessageDialog(this,
                    "Kaydınız başarıyla oluşturuldu!\nMüşteri ID: " + customer.getCustomerId(),
                    "Kayıt Başarılı",
                    JOptionPane.INFORMATION_MESSAGE);

            clearFields();
            mainFrame.showDashboard(customer);

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Kayıt Hatası",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        passwordField.setText("");
    }
}
