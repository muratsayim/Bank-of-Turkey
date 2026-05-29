package com.bank.ui;

import com.bank.model.Customer;
import com.bank.service.BankingService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginPanel extends JPanel {
    private final MainFrame mainFrame;
    private JTextField emailField;
    private JPasswordField passwordField;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        // 1. LEFT SIDE - Branding with Gradient and Highlights
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gorgeous dark navy-to-blue gradient
                GradientPaint gp = new GradientPaint(0, 0, new Color(11, 25, 44), 
                                                     getWidth(), getHeight(), new Color(30, 62, 98));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Decorative abstract light circles for visual texture
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillOval(-100, -100, 400, 400);
                g2.fillOval(getWidth() - 250, getHeight() - 250, 500, 500);
            }
        };
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(420, 650));
        leftPanel.setBorder(new EmptyBorder(60, 40, 60, 40));

        // Brand Icon & Title
        JLabel logoLabel = new JLabel("🏦");
        logoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brandTitle = new JLabel("BANK OF TURKEY");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        brandTitle.setForeground(Color.WHITE);
        brandTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brandSubtitle = new JLabel("Güvenli ve Modern Dijital Bankacılık");
        brandSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        brandSubtitle.setForeground(new Color(200, 214, 229));
        brandSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(logoLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        leftPanel.add(brandTitle);
        leftPanel.add(brandSubtitle);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 50)));

        // Feature Highlights
        addFeatureRow(leftPanel, "✓   7/24 Güvenli Para Transferi (Havale/EFT)");
        addFeatureRow(leftPanel, "✓   Yüksek Faiz Getirili Vadeli Hesaplar");
        addFeatureRow(leftPanel, "✓   Tek Tıkla Hesap Bilgisi Kopyalama ve Yapıştırma");
        addFeatureRow(leftPanel, "✓   Güçlü Şifreleme ve SHA-256 Veri Koruması");

        leftPanel.add(Box.createVerticalGlue());

        JLabel footerLabel = new JLabel("© 2026 Bank of Turkey. Tüm hakları saklıdır.");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLabel.setForeground(new Color(150, 150, 150));
        footerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(footerLabel);

        // 2. RIGHT SIDE - Login Form (Centered inside a GridBagLayout panel)
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(new Color(30, 30, 30)); // Standard dark theme

        JPanel formCard = new JPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(55, 60, 65), 1, true),
                new EmptyBorder(40, 40, 40, 40)
        ));
        formCard.setPreferredSize(new Dimension(380, 450));
        formCard.setMaximumSize(new Dimension(380, 450));
        formCard.setBackground(new Color(36, 40, 44));

        JLabel loginTitle = new JLabel("Hesabınıza Giriş Yapın");
        loginTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        loginTitle.setForeground(new Color(52, 152, 219));
        loginTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel loginSubtitle = new JLabel("Bilgilerinizi girerek devam edin");
        loginSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        loginSubtitle.setForeground(Color.GRAY);
        loginSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        formCard.add(loginTitle);
        formCard.add(Box.createRigidArea(new Dimension(0, 5)));
        formCard.add(loginSubtitle);
        formCard.add(Box.createRigidArea(new Dimension(0, 35)));

        // Email field
        JLabel emailLabel = new JLabel("E-posta Adresi");
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailField = new JTextField();
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailField.setMaximumSize(new Dimension(300, 38));
        emailField.setPreferredSize(new Dimension(300, 38));
        emailField.putClientProperty("JTextField.showClearButton", true);

        formCard.add(emailLabel);
        formCard.add(Box.createRigidArea(new Dimension(0, 6)));
        formCard.add(emailField);
        formCard.add(Box.createRigidArea(new Dimension(0, 20)));

        // Password field
        JLabel passwordLabel = new JLabel("Şifre");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setMaximumSize(new Dimension(300, 38));
        passwordField.setPreferredSize(new Dimension(300, 38));
        passwordField.putClientProperty("JTextField.showClearButton", true);

        formCard.add(passwordLabel);
        formCard.add(Box.createRigidArea(new Dimension(0, 6)));
        formCard.add(passwordField);
        formCard.add(Box.createRigidArea(new Dimension(0, 30)));

        // Buttons
        JButton loginButton = new JButton("Giriş Yap");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(new Color(41, 128, 185)); // Neon blue primary button
        loginButton.setMaximumSize(new Dimension(300, 42));
        loginButton.setPreferredSize(new Dimension(300, 42));
        loginButton.setFocusPainted(false);
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> handleLogin());

        JButton registerButton = new JButton("Yeni Hesap Oluştur (Kayıt Ol)");
        registerButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        registerButton.setForeground(new Color(52, 152, 219));
        registerButton.setContentAreaFilled(false);
        registerButton.setBorderPainted(false);
        registerButton.setFocusPainted(false);
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.addActionListener(e -> {
            clearFields();
            mainFrame.showRegister();
        });

        formCard.add(loginButton);
        formCard.add(Box.createRigidArea(new Dimension(0, 10)));
        formCard.add(registerButton);

        rightPanel.add(formCard);

        // Add both sides to Main panel
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

    private void addFeatureRow(JPanel parent, String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(220, 225, 230));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(label);
        parent.add(Box.createRigidArea(new Dimension(0, 15)));
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Lütfen tüm alanları doldurun.", 
                    "Eksik Bilgi", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        BankingService service = mainFrame.getBankingService();
        Customer customer = service.loginCustomer(email, password);

        if (customer != null) {
            clearFields();
            mainFrame.showDashboard(customer);
        } else {
            JOptionPane.showMessageDialog(this, 
                    "E-posta veya şifre hatalı. Lütfen tekrar deneyin.", 
                    "Giriş Başarısız", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        emailField.setText("");
        passwordField.setText("");
    }
}
