package com.bank;

import com.bank.ui.MainFrame;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        // Set default locale to Turkish for fully localized JOptionPane buttons (Evet/Hayır/Tamam)
        java.util.Locale.setDefault(new java.util.Locale("tr", "TR"));

        // FlatLaf Dark temasını kur
        try {
            FlatDarkLaf.setup();
            // Bazı UI bileşenlerini özelleştir
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("ProgressBar.arc", 12);

            // Explicitly force JOptionPane button translations in Turkish
            UIManager.put("OptionPane.yesButtonText", "Evet");
            UIManager.put("OptionPane.noButtonText", "Hayır");
            UIManager.put("OptionPane.cancelButtonText", "İptal");
            UIManager.put("OptionPane.okButtonText", "Tamam");
        } catch (Exception e) {
            System.err.println("FlatLaf teması yüklenemedi: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
