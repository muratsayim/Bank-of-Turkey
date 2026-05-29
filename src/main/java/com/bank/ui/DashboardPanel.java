package com.bank.ui;

import com.bank.model.Account;
import com.bank.model.Customer;
import com.bank.model.SavingsAccount;
import com.bank.model.Transaction;
import com.bank.service.BankingService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DashboardPanel extends JPanel {
    private final MainFrame mainFrame;
    private final Customer customer;
    private final BankingService service;

    // Right-side card layout
    private final CardLayout contentCardLayout;
    private final JPanel contentPanel;

    // Components to update dynamically
    private JTable accountsTable;
    private JTable transactionsTable;
    private JComboBox<String> depositWithdrawCombo;
    private JComboBox<String> transferSourceCombo;
    private JComboBox<String> interestCombo;
    private JComboBox<String> historyCombo;

    // Rich Dashboard Widgets Labels
    private JLabel totalAssetsValLabel;
    private JLabel savingsAssetsValLabel;
    private JLabel accountCountValLabel;
    private JLabel lastTxDescLabel;
    private AssetAllocationChart assetChart;

    public DashboardPanel(MainFrame mainFrame, Customer customer) {
        this.mainFrame = mainFrame;
        this.customer = customer;
        this.service = mainFrame.getBankingService();

        setLayout(new BorderLayout());

        // Left sidebar navigation with Emojis
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Right content area
        contentCardLayout = new CardLayout();
        contentPanel = new JPanel(contentCardLayout);
        contentPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Create right-side views
        contentPanel.add(createAccountsView(), "ACCOUNTS");
        contentPanel.add(createOpenAccountView(), "OPEN_ACCOUNT");
        contentPanel.add(createDepositWithdrawView(), "DEPOSIT_WITHDRAW");
        contentPanel.add(createTransferView(), "TRANSFER");
        contentPanel.add(createTransactionsView(), "TRANSACTIONS");
        contentPanel.add(createInterestView(), "INTEREST");

        add(contentPanel, BorderLayout.CENTER);

        // Initial view
        showView("ACCOUNTS");
    }

    private void showView(String cardName) {
        refreshData();
        contentCardLayout.show(contentPanel, cardName);
    }

    private void refreshData() {
        // Calculate variables
        double totalChecking = 0;
        double totalSavings = 0;
        int activeAccounts = 0;

        List<Account> accounts = customer.getAccounts();
        for (Account acc : accounts) {
            if (acc.isActive()) {
                activeAccounts++;
                if (acc.getAccountType() == Account.AccountType.CHECKING) {
                    totalChecking += acc.getBalance();
                } else {
                    totalSavings += acc.getBalance();
                }
            }
        }

        double totalAssets = totalChecking + totalSavings;

        // Refresh widgets labels if on main screen
        if (totalAssetsValLabel != null) {
            totalAssetsValLabel.setText(String.format("%,.2f TL", totalAssets));
            savingsAssetsValLabel.setText(String.format("%,.2f TL", totalSavings));
            accountCountValLabel.setText(activeAccounts + " Aktif Hesap");
        }

        // Refresh Chart
        if (assetChart != null) {
            assetChart.setBalances(totalChecking, totalSavings);
        }

        // Fetch Last Transaction
        if (lastTxDescLabel != null) {
            List<Transaction> allTxs = service.getTransactionHistory(""); // Get all transactions if possible
            // Wait, getTransactionHistory requires account number. Let's find last transaction from all accounts.
            Transaction lastTx = null;
            for (Account acc : accounts) {
                List<Transaction> txs = service.getTransactionHistory(acc.getAccountNumber());
                if (!txs.isEmpty()) {
                    Transaction localLast = txs.get(txs.size() - 1);
                    if (lastTx == null || localLast.getTimestamp().compareTo(lastTx.getTimestamp()) > 0) {
                        lastTx = localLast;
                    }
                }
            }
            if (lastTx != null) {
                String typeStr = lastTx.getType() == Transaction.TransactionType.DEPOSIT ? "Para Yatırma" :
                                (lastTx.getType() == Transaction.TransactionType.WITHDRAWAL ? "Para Çekme" : "Transfer");
                lastTxDescLabel.setText(String.format("<html><b>%s</b><br>Tutar: %.2f TL<br>%s</html>", 
                        typeStr, lastTx.getAmount(), lastTx.getDescription()));
            } else {
                lastTxDescLabel.setText("İşlem geçmişi henüz temiz.");
            }
        }

        // Refresh Accounts Table
        DefaultTableModel model = (DefaultTableModel) accountsTable.getModel();
        model.setRowCount(0);
        for (Account acc : accounts) {
            String interest = "-";
            if (acc instanceof SavingsAccount) {
                interest = String.format("%% %.1f", ((SavingsAccount) acc).getInterestRate() * 100);
            }
            model.addRow(new Object[]{
                    acc.getAccountNumber(),
                    acc.getAccountType() == Account.AccountType.CHECKING ? "Vadesiz (Checking)" : "Vadeli (Savings)",
                    String.format("%.2f TL", acc.getBalance()),
                    interest,
                    acc.isActive() ? "Aktif" : "Pasif"
            });
        }

        // Refresh Combos
        updateCombos();
    }

    private void updateCombos() {
        depositWithdrawCombo.removeAllItems();
        transferSourceCombo.removeAllItems();
        interestCombo.removeAllItems();
        historyCombo.removeAllItems();

        for (Account acc : customer.getAccounts()) {
            String item = acc.getAccountNumber() + " (" + (acc.getAccountType() == Account.AccountType.CHECKING ? "Vadesiz" : "Vadeli") + " - " + String.format("%.2f TL", acc.getBalance()) + ")";
            depositWithdrawCombo.addItem(item);
            transferSourceCombo.addItem(item);
            historyCombo.addItem(item);

            if (acc.getAccountType() == Account.AccountType.SAVINGS) {
                interestCombo.addItem(item);
            }
        }
    }

    private String getSelectedAccountNumber(JComboBox<String> combo) {
        String selected = (String) combo.getSelectedItem();
        if (selected == null) return null;
        return selected.split(" ")[0];
    }

    // --- VIEW GENERATORS ---

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(245, 650));
        sidebar.setBackground(new Color(25, 27, 30)); // Deep dark premium grey
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));

        // Bank Title & Icon
        JLabel brandIcon = new JLabel("🏦 BANK OF TURKEY");
        brandIcon.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandIcon.setForeground(new Color(52, 152, 219));
        brandIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Profile Section Card
        JPanel profileCard = new JPanel();
        profileCard.setLayout(new BoxLayout(profileCard, BoxLayout.Y_AXIS));
        profileCard.setBackground(new Color(36, 40, 44));
        profileCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 55, 60), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));
        profileCard.setMaximumSize(new Dimension(215, 80));
        profileCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(customer.getFullName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel infoLabel = new JLabel("Müşteri No: " + customer.getCustomerId());
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        profileCard.add(nameLabel);
        profileCard.add(Box.createRigidArea(new Dimension(0, 5)));
        profileCard.add(infoLabel);

        sidebar.add(brandIcon);
        sidebar.add(Box.createRigidArea(new Dimension(0, 25)));
        sidebar.add(profileCard);
        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));

        // Navigation Buttons with Emojis
        addSidebarButton(sidebar, "💳   Hesaplarım", "ACCOUNTS");
        addSidebarButton(sidebar, "➕   Yeni Hesap Aç", "OPEN_ACCOUNT");
        addSidebarButton(sidebar, "📥   Para Yatır / Çek", "DEPOSIT_WITHDRAW");
        addSidebarButton(sidebar, "💸   Para Transferi", "TRANSFER");
        addSidebarButton(sidebar, "📜   İşlem Geçmişi", "TRANSACTIONS");
        addSidebarButton(sidebar, "📈   Vadeli Faiz İşlet", "INTEREST");

        sidebar.add(Box.createVerticalGlue());

        // Logout Button
        JButton logoutBtn = new JButton("🚪 Güvenli Çıkış");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(new Color(192, 57, 43));
        logoutBtn.setMaximumSize(new Dimension(205, 38));
        logoutBtn.setPreferredSize(new Dimension(205, 38));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                    "Güvenli çıkış yapmak istediğinize emin misiniz?", 
                    "Oturumu Kapat", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                mainFrame.showLogin();
            }
        });
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private void addSidebarButton(JPanel parent, String labelText, String cardName) {
        JButton btn = new JButton(labelText);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(new Color(180, 185, 190));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(205, 38));
        btn.setPreferredSize(new Dimension(205, 38));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> showView(cardName));
        parent.add(btn);
        parent.add(Box.createRigidArea(new Dimension(0, 6)));
    }

    // 1. Premium Dashboard Accounts View (Landing view)
    private JPanel createAccountsView() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));

        // Welcome Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Merhaba, " + customer.getFullName() + "! 👋");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcomeLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Hesap durumunuza ve varlık dağılımınıza buradan ulaşabilirsiniz.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.GRAY);

        headerPanel.add(welcomeLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.CENTER);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Grid panel for top widgets/cards
        JPanel widgetsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        widgetsPanel.setPreferredSize(new Dimension(0, 95));

        // Widget 1: Total Assets
        JPanel w1 = createWidgetCard("TOPLAM VARLIKLARIM", "0.00 TL", new Color(41, 128, 185));
        totalAssetsValLabel = (JLabel) w1.getClientProperty("valueLabel");
        
        // Widget 2: Savings Assets
        JPanel w2 = createWidgetCard("VADELİ BİRİKİM", "0.00 TL", new Color(46, 204, 113));
        savingsAssetsValLabel = (JLabel) w2.getClientProperty("valueLabel");

        // Widget 3: Account Count
        JPanel w3 = createWidgetCard("HESAP DURUMU", "0 Aktif Hesap", new Color(241, 196, 15));
        accountCountValLabel = (JLabel) w3.getClientProperty("valueLabel");

        widgetsPanel.add(w1);
        widgetsPanel.add(w2);
        widgetsPanel.add(w3);

        // Center Panel: Chart & Last transaction row
        JPanel centerPanel = new JPanel(new BorderLayout(15, 0));

        // Left Side: Doughnut Chart
        JPanel chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBorder(BorderFactory.createTitledBorder("Varlık Dağılım Grafiği"));
        chartContainer.setPreferredSize(new Dimension(300, 200));
        assetChart = new AssetAllocationChart();
        chartContainer.add(assetChart, BorderLayout.CENTER);

        // Right Side: Quick info / Last Transaction Card
        JPanel lastTxCard = new JPanel();
        lastTxCard.setLayout(new BoxLayout(lastTxCard, BoxLayout.Y_AXIS));
        lastTxCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Son Gerçekleşen Hareket"),
                new EmptyBorder(15, 15, 15, 15)
        ));
        
        lastTxDescLabel = new JLabel("İşlem geçmişi henüz temiz.");
        lastTxDescLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lastTxDescLabel.setForeground(Color.LIGHT_GRAY);
        lastTxCard.add(lastTxDescLabel);

        centerPanel.add(chartContainer, BorderLayout.WEST);
        centerPanel.add(lastTxCard, BorderLayout.CENTER);

        // Combine widgets and center panel into a vertical layout panel
        JPanel topAndCenterWrapper = new JPanel(new BorderLayout(0, 15));
        topAndCenterWrapper.add(widgetsPanel, BorderLayout.NORTH);
        topAndCenterWrapper.add(centerPanel, BorderLayout.CENTER);
        panel.add(topAndCenterWrapper, BorderLayout.CENTER);

        // Bottom panel: Table list & copy
        JPanel bottomContainer = new JPanel(new BorderLayout(0, 5));
        bottomContainer.setPreferredSize(new Dimension(0, 220));
        
        String[] columnNames = {"Hesap Numarası", "Hesap Türü", "Bakiye", "Faiz Oranı", "Hesap Durumu"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        accountsTable = new JTable(model);
        accountsTable.setRowHeight(28);
        accountsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        accountsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        accountsTable.setShowGrid(true);
        accountsTable.setGridColor(new Color(50, 50, 50));

        JScrollPane scrollPane = new JScrollPane(accountsTable);
        bottomContainer.add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Panel for Copy Button
        JPanel bottomActionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        JButton copyBtn = new JButton("📋 Seçili Hesap Numarasını (IBAN) Kopyala");
        copyBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        copyBtn.putClientProperty("Button.arc", 8);
        copyBtn.addActionListener(e -> {
            int selectedRow = accountsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, 
                        "Lütfen panoya kopyalamak istediğiniz hesabı seçin.", 
                        "Seçim Yapılmadı", 
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            String accountNum = (String) accountsTable.getValueAt(selectedRow, 0);
            try {
                java.awt.datatransfer.StringSelection stringSelection = new java.awt.datatransfer.StringSelection(accountNum);
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
                JOptionPane.showMessageDialog(this, 
                        "Hesap Numarası (" + accountNum + ") başarıyla panoya kopyalandı!", 
                        "Kopyalandı", 
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                        "Kopyalama esnasında bir hata oluştu.", 
                        "Hata", 
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        bottomActionRow.add(copyBtn);
        bottomContainer.add(bottomActionRow, BorderLayout.SOUTH);

        panel.add(bottomContainer, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createWidgetCard(String title, String val, Color accentColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(36, 40, 44));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(55, 60, 65), 1, true),
                new EmptyBorder(12, 18, 12, 18)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLabel.setForeground(Color.GRAY);

        JLabel valLabel = new JLabel(val);
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valLabel.setForeground(accentColor);

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(valLabel);

        // Put a client property reference so we can easily change values later
        card.putClientProperty("valueLabel", valLabel);

        return card;
    }

    // 2. Open Account View
    private JPanel createOpenAccountView() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel header = new JLabel("YENİ HESAP AÇILIŞI ➕");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(new Color(52, 152, 219));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(header);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        JLabel typeLabel = new JLabel("Hesap Türü");
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(typeLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));

        String[] types = {"Vadesiz (Checking) Hesap", "Vadeli (Savings) Hesap"};
        JComboBox<String> typeCombo = new JComboBox<>(types);
        typeCombo.setMaximumSize(new Dimension(400, 36));
        typeCombo.setPreferredSize(new Dimension(400, 36));
        typeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(typeCombo);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel balanceLabel = new JLabel("Başlangıç Bakiyesi (TL)");
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        balanceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(balanceLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));

        JTextField balanceField = new JTextField("0.0");
        balanceField.setMaximumSize(new Dimension(400, 36));
        balanceField.setPreferredSize(new Dimension(400, 36));
        balanceField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(balanceField);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel interestLabel = new JLabel("Faiz Oranı (Sadece Vadeli Hesap İçin. Örn: 0.15)");
        interestLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        interestLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(interestLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));

        JTextField interestField = new JTextField("0.15");
        interestField.setMaximumSize(new Dimension(400, 36));
        interestField.setPreferredSize(new Dimension(400, 36));
        interestField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(interestField);
        panel.add(Box.createRigidArea(new Dimension(0, 35)));

        // Submit Button
        JButton openBtn = new JButton("Hesabı Oluştur");
        openBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        openBtn.setForeground(Color.WHITE);
        openBtn.setBackground(new Color(41, 128, 185));
        openBtn.setMaximumSize(new Dimension(200, 40));
        openBtn.setPreferredSize(new Dimension(200, 40));
        openBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        openBtn.addActionListener(e -> {
            try {
                Account.AccountType type = typeCombo.getSelectedIndex() == 0 ? Account.AccountType.CHECKING : Account.AccountType.SAVINGS;
                String initialBalStr = balanceField.getText().trim();
                String interestStr = interestField.getText().trim();
                if (!isValidAmountString(initialBalStr) || !isValidAmountString(interestStr)) {
                    throw new NumberFormatException();
                }
                double initialBalance = Double.parseDouble(initialBalStr);
                double interest = Double.parseDouble(interestStr);

                Account acc = service.createAccount(customer, type, initialBalance, interest);
                JOptionPane.showMessageDialog(this, "Hesabınız başarıyla açıldı!\nHesap Numarası: " + acc.getAccountNumber());
                balanceField.setText("0.0");
                showView("ACCOUNTS");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                        "Lütfen bakiye ve faiz alanlarına sadece geçerli sayılar giriniz (örn: 100.50 veya 0.15).", 
                        "Geçersiz Sayı Formatı", 
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(openBtn);

        return panel;
    }

    // 3. Deposit / Withdraw View
    private JPanel createDepositWithdrawView() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel header = new JLabel("PARA YATIRMA VE ÇEKME İŞLEMLERİ 📥");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(new Color(52, 152, 219));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(header);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        JLabel selectLabel = new JLabel("İşlem Yapılacak Hesap");
        selectLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        selectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(selectLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));

        depositWithdrawCombo = new JComboBox<>();
        depositWithdrawCombo.setMaximumSize(new Dimension(450, 36));
        depositWithdrawCombo.setPreferredSize(new Dimension(450, 36));
        depositWithdrawCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(depositWithdrawCombo);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel opLabel = new JLabel("İşlem Türü");
        opLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        opLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(opLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));

        String[] ops = {"Para Yatır (Deposit)", "Para Çek (Withdraw)"};
        JComboBox<String> opCombo = new JComboBox<>(ops);
        opCombo.setMaximumSize(new Dimension(450, 36));
        opCombo.setPreferredSize(new Dimension(450, 36));
        opCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(opCombo);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel amountLabel = new JLabel("İşlem Tutarı (TL)");
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        amountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(amountLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));

        JTextField amountField = new JTextField();
        amountField.setMaximumSize(new Dimension(450, 36));
        amountField.setPreferredSize(new Dimension(450, 36));
        amountField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(amountField);
        panel.add(Box.createRigidArea(new Dimension(0, 35)));

        // Submit Button
        JButton actionBtn = new JButton("İşlemi Onayla");
        actionBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        actionBtn.setForeground(Color.WHITE);
        actionBtn.setBackground(new Color(46, 204, 113));
        actionBtn.setMaximumSize(new Dimension(200, 40));
        actionBtn.setPreferredSize(new Dimension(200, 40));
        actionBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionBtn.addActionListener(e -> {
            String accNum = getSelectedAccountNumber(depositWithdrawCombo);
            if (accNum == null) {
                JOptionPane.showMessageDialog(this, "Lütfen bir hesap seçin.", "Hata", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                String amountStr = amountField.getText().trim();
                if (!isValidAmountString(amountStr)) {
                    throw new NumberFormatException();
                }
                double amount = Double.parseDouble(amountStr);
                if (opCombo.getSelectedIndex() == 0) {
                    service.deposit(customer, accNum, amount);
                    JOptionPane.showMessageDialog(this, amount + " TL hesaba başarıyla yatırıldı.");
                } else {
                    service.withdraw(customer, accNum, amount);
                    JOptionPane.showMessageDialog(this, amount + " TL hesaptan başarıyla çekildi.");
                }
                amountField.setText("");
                showView("ACCOUNTS");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                        "Lütfen geçerli bir tutar giriniz (örn: 100.50).", 
                        "Geçersiz Sayı Formatı", 
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(actionBtn);

        return panel;
    }

    // 4. Transfer View
    private JPanel createTransferView() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel header = new JLabel("HESAPLAR ARASI PARA TRANSFERİ 💸");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(new Color(52, 152, 219));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(header);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        JLabel srcLabel = new JLabel("Gönderen Hesap");
        srcLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        srcLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(srcLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));

        transferSourceCombo = new JComboBox<>();
        transferSourceCombo.setMaximumSize(new Dimension(450, 36));
        transferSourceCombo.setPreferredSize(new Dimension(450, 36));
        transferSourceCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(transferSourceCombo);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel dstLabel = new JLabel("Alıcı Hesap Numarası (TR...)");
        dstLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        dstLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(dstLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));

        // Horizontal row for text field and paste button
        JPanel dstRow = new JPanel();
        dstRow.setLayout(new BoxLayout(dstRow, BoxLayout.X_AXIS));
        dstRow.setMaximumSize(new Dimension(450, 36));
        dstRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        final JTextField dstField = new JTextField();
        dstField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dstField.putClientProperty("JTextField.showClearButton", true);
        dstRow.add(dstField);
        dstRow.add(Box.createRigidArea(new Dimension(10, 0)));

        JButton pasteBtn = new JButton("Panodan Yapıştır");
        pasteBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        pasteBtn.putClientProperty("Button.arc", 8);
        pasteBtn.addActionListener(ev -> {
            try {
                java.awt.datatransfer.Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
                java.awt.datatransfer.Transferable transferable = clipboard.getContents(null);
                if (transferable != null && transferable.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.stringFlavor)) {
                    String text = (String) transferable.getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor);
                    if (text != null) {
                        dstField.setText(text.trim());
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                        "Panodan yapıştırma esnasında bir hata oluştu.", 
                        "Hata", 
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        dstRow.add(pasteBtn);

        panel.add(dstRow);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel amountLabel = new JLabel("Gönderilecek Tutar (TL)");
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        amountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(amountLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));

        JTextField amountField = new JTextField();
        amountField.setMaximumSize(new Dimension(450, 36));
        amountField.setPreferredSize(new Dimension(450, 36));
        amountField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(amountField);
        panel.add(Box.createRigidArea(new Dimension(0, 35)));

        // Submit Button
        JButton transferBtn = new JButton("Transferi Gerçekleştir");
        transferBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        transferBtn.setForeground(Color.WHITE);
        transferBtn.setBackground(new Color(41, 128, 185));
        transferBtn.setMaximumSize(new Dimension(200, 40));
        transferBtn.setPreferredSize(new Dimension(200, 40));
        transferBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        transferBtn.addActionListener(e -> {
            String srcAcc = getSelectedAccountNumber(transferSourceCombo);
            String dstAcc = dstField.getText().trim();
            if (srcAcc == null || dstAcc.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lütfen gönderen ve alıcı hesap bilgilerini doldurun.", "Hata", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                String amountStr = amountField.getText().trim();
                if (!isValidAmountString(amountStr)) {
                    throw new NumberFormatException();
                }
                double amount = Double.parseDouble(amountStr);
                service.transfer(customer, srcAcc, dstAcc, amount);
                JOptionPane.showMessageDialog(this, "Transfer işlemi başarıyla tamamlandı!");
                dstField.setText("");
                amountField.setText("");
                showView("ACCOUNTS");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                        "Lütfen geçerli bir transfer tutarı giriniz (örn: 100.50).", 
                        "Geçersiz Sayı Formatı", 
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(transferBtn);

        return panel;
    }

    // 5. Transaction History View
    private JPanel createTransactionsView() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel header = new JLabel("İŞLEM GEÇMİŞİ 📜");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(new Color(52, 152, 219));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(header);
        topPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel selectorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        selectorRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel selectLabel = new JLabel("Hesap Seçin:   ");
        selectLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        selectorRow.add(selectLabel);

        historyCombo = new JComboBox<>();
        historyCombo.setPreferredSize(new Dimension(300, 32));
        historyCombo.addActionListener(e -> loadTransactionHistory());
        selectorRow.add(historyCombo);

        topPanel.add(selectorRow);
        panel.add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"İşlem ID", "Kaynak Hesap", "Alıcı Hesap", "Tutar", "İşlem Tipi", "Açıklama"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        transactionsTable = new JTable(model);
        transactionsTable.setRowHeight(28);
        transactionsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        transactionsTable.setShowGrid(true);
        transactionsTable.setGridColor(new Color(50, 50, 50));

        // Adjust column widths so that descriptions fit perfectly
        transactionsTable.getColumnModel().getColumn(0).setPreferredWidth(80);   // İşlem ID
        transactionsTable.getColumnModel().getColumn(1).setPreferredWidth(130);  // Kaynak Hesap
        transactionsTable.getColumnModel().getColumn(2).setPreferredWidth(130);  // Alıcı Hesap
        transactionsTable.getColumnModel().getColumn(3).setPreferredWidth(100);  // Tutar
        transactionsTable.getColumnModel().getColumn(4).setPreferredWidth(110);  // İşlem Tipi
        transactionsTable.getColumnModel().getColumn(5).setPreferredWidth(320);  // Açıklama

        JScrollPane scrollPane = new JScrollPane(transactionsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadTransactionHistory() {
        String accNum = getSelectedAccountNumber(historyCombo);
        DefaultTableModel model = (DefaultTableModel) transactionsTable.getModel();
        model.setRowCount(0);

        if (accNum == null) return;

        List<Transaction> history = service.getTransactionHistory(accNum);
        for (Transaction t : history) {
            String src = t.getSourceAccountNumber() != null ? t.getSourceAccountNumber() : "DIŞARIDAN";
            String dst = t.getTargetAccountNumber() != null ? t.getTargetAccountNumber() : "DIŞARIYA";
            model.addRow(new Object[]{
                    t.getId(),
                    src,
                    dst,
                    String.format("%.2f TL", t.getAmount()),
                    t.getType().name(),
                    t.getDescription()
            });
        }
    }

    // 6. Apply Simulated Interest View
    private JPanel createInterestView() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel header = new JLabel("VADELİ HESAP FAİZ İŞLETİMİ 📈");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(new Color(52, 152, 219));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(header);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        JLabel selectLabel = new JLabel("İşlem Yapılacak Vadeli Hesap");
        selectLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        selectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(selectLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));

        interestCombo = new JComboBox<>();
        interestCombo.setMaximumSize(new Dimension(450, 36));
        interestCombo.setPreferredSize(new Dimension(450, 36));
        interestCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(interestCombo);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel daysLabel = new JLabel("Faiz Uygulanacak Gün Sayısı");
        daysLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        daysLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(daysLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));

        JTextField daysField = new JTextField("30");
        daysField.setMaximumSize(new Dimension(450, 36));
        daysField.setPreferredSize(new Dimension(450, 36));
        daysField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(daysField);
        panel.add(Box.createRigidArea(new Dimension(0, 35)));

        // Submit Button
        JButton calcBtn = new JButton("Faiz İşlet ve Ekle");
        calcBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        calcBtn.setForeground(Color.WHITE);
        calcBtn.setBackground(new Color(46, 204, 113));
        calcBtn.setMaximumSize(new Dimension(200, 40));
        calcBtn.setPreferredSize(new Dimension(200, 40));
        calcBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        calcBtn.addActionListener(e -> {
            String accNum = getSelectedAccountNumber(interestCombo);
            if (accNum == null) {
                JOptionPane.showMessageDialog(this, "Lütfen aktif bir Vadeli Hesap seçin.", "Hata", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                String daysStr = daysField.getText().trim();
                if (!isValidIntegerString(daysStr)) {
                    throw new NumberFormatException();
                }
                int days = Integer.parseInt(daysStr);
                Account acc = customer.findAccount(accNum);
                double before = acc.getBalance();
                service.applyInterest(customer, accNum, days);
                double after = acc.getBalance();
                double earned = after - before;

                JOptionPane.showMessageDialog(this,
                        String.format("%d günlük faiz uygulandı!\nKazanılan Faiz: %.2f TL\nYeni Bakiye: %.2f TL", days, earned, after),
                        "Faiz Başarıyla Eklendi",
                        JOptionPane.INFORMATION_MESSAGE);

                daysField.setText("30");
                showView("ACCOUNTS");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                        "Lütfen geçerli bir gün sayısı giriniz (tam sayı olmalıdır).", 
                        "Geçersiz Gün Değeri", 
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(calcBtn);

        return panel;
    }

    private boolean isValidAmountString(String text) {
        return text != null && text.matches("^[0-9]+(\\.[0-9]+)?$");
    }

    private boolean isValidIntegerString(String text) {
        return text != null && text.matches("^[0-9]+$");
    }

    // --- CUSTOM PIE/DOUGHNUT CHART DRAWING PANEL ---
    private static class AssetAllocationChart extends JPanel {
        private double checkingBalance = 0;
        private double savingsBalance = 0;

        public void setBalances(double checking, double savings) {
            this.checkingBalance = checking;
            this.savingsBalance = savings;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int size = Math.min(width, height) - 70;
            int x = (width - size) / 2;
            int y = (height - size) / 2;

            double total = checkingBalance + savingsBalance;
            if (total == 0) {
                // Draw empty grey circle
                g2.setColor(new Color(60, 64, 68));
                g2.fillOval(x, y, size, size);
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2.drawString("Aktif Bakiye Yok", x + size / 2 - 40, y + size / 2 + 5);
                return;
            }

            int checkingAngle = (int) Math.round((checkingBalance / total) * 360);
            int savingsAngle = 360 - checkingAngle;

            // Draw checking slice (Neon Blue) starting from the top (-90 degrees)
            g2.setColor(new Color(41, 128, 185));
            g2.fillArc(x, y, size, size, -90, checkingAngle);

            // Draw savings slice (Neon Green) starting after checking
            g2.setColor(new Color(46, 204, 113));
            g2.fillArc(x, y, size, size, -90 + checkingAngle, savingsAngle);

            // Center cutout (Doughnut effect)
            int centerSize = (int) (size * 0.62);
            int cx = (width - centerSize) / 2;
            int cy = (height - centerSize) / 2;
            g2.setColor(new Color(43, 43, 43)); // Matches FlatDarkLaf panel background
            g2.fillOval(cx, cy, centerSize, centerSize);

            // Legends at bottom
            int legendY = height - 30;
            
            // Checking Legend
            g2.setColor(new Color(41, 128, 185));
            g2.fillRect(15, legendY, 12, 12);
            g2.setColor(Color.LIGHT_GRAY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.drawString(String.format("Vadesiz: %.0f%%", (checkingBalance / total) * 100), 32, legendY + 10);

            // Savings Legend
            g2.setColor(new Color(46, 204, 113));
            g2.fillRect(145, legendY, 12, 12);
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawString(String.format("Vadeli: %.0f%%", (savingsBalance / total) * 100), 162, legendY + 10);
        }
    }
}
