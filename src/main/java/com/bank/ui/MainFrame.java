package com.bank.ui;

import com.bank.model.Customer;
import com.bank.service.BankingService;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private final BankingService bankingService;

    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private DashboardPanel dashboardPanel;

    public MainFrame() {
        this.bankingService = new BankingService();
        this.cardLayout = new CardLayout();
        this.mainPanel = new JPanel(cardLayout);

        // JFrame configuration
        setTitle("Bank of Turkey - Güvenli İnternet Bankacılığı");
        setSize(1000, 650);
        setMinimumSize(new Dimension(850, 550));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center window

        // Initialize panels
        initPanels();

        // Add to frame
        add(mainPanel);

        // Start with login view
        showLogin();
    }

    private void initPanels() {
        loginPanel = new LoginPanel(this);
        registerPanel = new RegisterPanel(this);
        // DashboardPanel is initialized dynamically upon login to load the fresh customer data

        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(registerPanel, "REGISTER");
    }

    public BankingService getBankingService() {
        return bankingService;
    }

    public void showLogin() {
        cardLayout.show(mainPanel, "LOGIN");
    }

    public void showRegister() {
        cardLayout.show(mainPanel, "REGISTER");
    }

    public void showDashboard(Customer customer) {
        if (dashboardPanel != null) {
            mainPanel.remove(dashboardPanel);
        }
        dashboardPanel = new DashboardPanel(this, customer);
        mainPanel.add(dashboardPanel, "DASHBOARD");
        cardLayout.show(mainPanel, "DASHBOARD");
    }
}
