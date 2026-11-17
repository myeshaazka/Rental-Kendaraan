package com.rentalkendaraan;

import java.awt.Font;
import java.util.Enumeration;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.rentalkendaraan.controller.RentalController;
import com.rentalkendaraan.view.AppTheme;
import com.rentalkendaraan.view.MainFrame;

/**
 * Entry point aplikasi desktop Sistem Manajemen Rental Kendaraan.
 *
 * <p>Jalankan kelas ini dari Eclipse: klik kanan {@code Main.java} &gt;
 * Run As &gt; Java Application.</p>
 */
public class Main {

    public static void main(String[] args) {
        // Aktifkan anti-aliasing font tingkat sistem agar teks tajam dan halus
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        pasangLookAndFeel();
        pasangPenangananErrorTakTerduga();

        SwingUtilities.invokeLater(() -> {
            RentalController controller = new RentalController();
            MainFrame frame = new MainFrame(controller);
            frame.setVisible(true);
        });
    }

    /**
     * Menangkap exception tak terduga di thread mana pun (terutama EDT) agar
     * aplikasi tidak "diam" tanpa penjelasan, melainkan menampilkan dialog.
     */
    private static void pasangPenangananErrorTakTerduga() {
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            error.printStackTrace();
            SwingUtilities.invokeLater(() -> javax.swing.JOptionPane.showMessageDialog(null,
                    "Terjadi kesalahan tak terduga:\n"
                            + (error.getMessage() == null
                                    ? error.getClass().getSimpleName()
                                    : error.getMessage()),
                    "Kesalahan Aplikasi", javax.swing.JOptionPane.ERROR_MESSAGE));
        });
    }

    /**
     * Memasang Look and Feel Sistem dengan penyesuaian font Segoe UI
     * agar tampilan menyatu dengan UI modern.
     */
    private static void pasangLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Terapkan font Segoe UI standar secara global untuk komponen default
            Font fontStandar = AppTheme.FONT_BODY;
            Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object val = UIManager.get(key);
                if (val instanceof Font) {
                    UIManager.put(key, fontStandar);
                }
            }

            // Atur warna latar belakang default JOptionPane agar senada
            UIManager.put("OptionPane.background", AppTheme.CARD_BG);
            UIManager.put("Panel.background", AppTheme.CARD_BG);
        } catch (Exception ex) {
            System.err.println("[Main] Gagal memasang Look and Feel: " + ex.getMessage());
        }
    }
}
