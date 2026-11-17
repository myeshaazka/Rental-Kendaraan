package com.rentalkendaraan.view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.rentalkendaraan.controller.RentalController;
import com.rentalkendaraan.exception.InputTidakValidException;

/**
 * Halaman login dengan desain kartu modern bertema Pastel Lavender.
 */
public class LoginPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final MainFrame frame;
    private final RentalController controller;

    private final AppTheme.ModernTextField txtUsername  = new AppTheme.ModernTextField(18);
    private final PasswordEyeField         txtPassword  = new PasswordEyeField(18);
    private final AppTheme.ModernButton    btnLogin     =
            new AppTheme.ModernButton("Masuk", AppTheme.HEADER, AppTheme.HEADER_HOVER);
    private final AppTheme.GhostButton     btnTesKoneksi =
            new AppTheme.GhostButton("Tes Koneksi Database", AppTheme.HEADER);

    public LoginPanel(MainFrame frame, RentalController controller) {
        this.frame      = frame;
        this.controller = controller;

        setLayout(new GridBagLayout());
        setBackground(AppTheme.BG_MAIN);
        add(buatKartuLogin());
        pasangAksi();
    }

    // ---------------------------------------------------------------
    private JPanel buatKartuLogin() {
        // Wrapper dengan shadow
        AppTheme.CardPanel kartu = new AppTheme.CardPanel(20, 8);
        kartu.setLayout(new BorderLayout(0, 0));
        kartu.setPreferredSize(new Dimension(460, 500));

        // Konten dalam kartu
        JPanel inner = new JPanel(new BorderLayout(0, 20));
        inner.setOpaque(false);
        inner.setBorder(BorderFactory.createEmptyBorder(32, 40, 28, 40));

        // ---- Header area dengan gradient kecil ----
        JPanel headerPanel = new HeaderBanner();
        headerPanel.setPreferredSize(new Dimension(0, 110));

        // ---- Form ----
        JPanel form = buatForm();

        // ---- Info footer ----
        JLabel lblInfo = new JLabel(
                "<html><div style='text-align:center;color:#8B8FA8;'>"
                + "Akun default: <b>admin</b> / <b>admin123</b><br>"
                + "<span style='font-size:10px;'>Pastikan konfigurasi src/db.properties sudah diisi</span>"
                + "</div></html>",
                SwingConstants.CENTER);
        lblInfo.setFont(AppTheme.FONT_SMALL);

        inner.add(headerPanel, BorderLayout.NORTH);
        inner.add(form,        BorderLayout.CENTER);
        inner.add(lblInfo,     BorderLayout.SOUTH);

        kartu.add(inner, BorderLayout.CENTER);
        return kartu;
    }

    private JPanel buatForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx   = 0;
        gbc.weightx = 1.0;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.anchor  = GridBagConstraints.WEST;

        // Username
        gbc.gridy  = 0;
        gbc.insets = new Insets(0, 0, 4, 0);
        form.add(AppTheme.makeFormLabel("USERNAME"), gbc);

        gbc.gridy  = 1;
        gbc.insets = new Insets(0, 0, 14, 0);
        form.add(txtUsername, gbc);

        // Password
        gbc.gridy  = 2;
        gbc.insets = new Insets(0, 0, 4, 0);
        form.add(AppTheme.makeFormLabel("PASSWORD"), gbc);

        gbc.gridy  = 3;
        gbc.insets = new Insets(0, 0, 20, 0);
        form.add(txtPassword, gbc);

        // Tombol Masuk
        gbc.gridy  = 4;
        gbc.insets = new Insets(0, 0, 10, 0);
        btnLogin.setPreferredSize(new Dimension(0, 42));
        form.add(btnLogin, gbc);

        // Tombol Tes Koneksi
        gbc.gridy  = 5;
        gbc.insets = new Insets(0, 0, 0, 0);
        btnTesKoneksi.setPreferredSize(new Dimension(0, 36));
        form.add(btnTesKoneksi, gbc);

        return form;
    }

    private void pasangAksi() {
        btnLogin.addActionListener(e -> prosesLogin());
        txtPassword.addActionListener(e -> prosesLogin());
        btnTesKoneksi.addActionListener(e -> tesKoneksi());
    }

    private void prosesLogin() {
        try {
            boolean berhasil = controller.login(txtUsername.getText(),
                    new String(txtPassword.getPassword()));
            if (berhasil) {
                txtPassword.setText("");
                frame.loginBerhasil();
            } else {
                UiHelper.tampilkanError(this,
                        "Username atau password salah.\n"
                        + "Gunakan akun default: admin / admin123");
                txtPassword.selectAll();
                txtPassword.requestFocusInWindow();
            }
        } catch (InputTidakValidException ex) {
            UiHelper.tampilkanError(this, ex.getMessage());
        }
    }

    private void tesKoneksi() {
        try {
            controller.tesKoneksi();
            UiHelper.tampilkanInfo(this,
                    "Koneksi ke database Supabase berhasil.\n\n"
                    + controller.infoKoneksi());
        } catch (SQLException ex) {
            UiHelper.tampilkanError(this, "Koneksi ke database gagal:\n" + ex.getMessage());
        }
    }

    /** Mengosongkan form (dipakai saat logout). */
    public void resetForm() {
        txtUsername.setText("");
        txtPassword.setText("");
        txtUsername.requestFocusInWindow();
    }

    // ---------------------------------------------------------------
    // INNER: Header banner dengan gradient
    // ---------------------------------------------------------------
    private static class HeaderBanner extends JPanel {
        private static final long serialVersionUID = 1L;

        HeaderBanner() {
            setOpaque(false);
            setLayout(new BorderLayout(0, 8));

            JPanel iconWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            iconWrapper.setOpaque(false);
            AppTheme.CarBrandBadge icon = new AppTheme.CarBrandBadge(
                    52, new Color(0xEDE9FF), AppTheme.HEADER);
            iconWrapper.add(icon);

            JLabel title = new JLabel("RENTAL KENDARAAN", SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI", Font.BOLD, 22));
            title.setForeground(AppTheme.TEXT_DARK);

            JLabel sub = new JLabel("Sistem Manajemen Rental Kendaraan", SwingConstants.CENTER);
            sub.setFont(AppTheme.FONT_BODY);
            sub.setForeground(AppTheme.TEXT_MUTED);

            JPanel texts = new JPanel(new BorderLayout(0, 4));
            texts.setOpaque(false);
            texts.add(title, BorderLayout.NORTH);
            texts.add(sub, BorderLayout.SOUTH);

            add(iconWrapper, BorderLayout.NORTH);
            add(texts,       BorderLayout.CENTER);
        }
    }

    // ---------------------------------------------------------------
    // INNER: PasswordEyeField (dengan tombol mata untuk lihat/sembunyikan)
    // ---------------------------------------------------------------
    public static class PasswordEyeField extends JPanel {
        private static final long serialVersionUID = 1L;
        private final JPasswordField txtPassword;
        private final JButton btnEye;
        private boolean passwordVisible = false;
        private boolean focused = false;
        private final char defaultEchoChar;

        public PasswordEyeField(int cols) {
            setLayout(new BorderLayout(0, 0));
            setOpaque(false);
            setBackground(new Color(0xF8F7FE));

            txtPassword = new JPasswordField(cols);
            txtPassword.setOpaque(false);
            txtPassword.setFont(AppTheme.FONT_BODY);
            txtPassword.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 4));
            txtPassword.setForeground(AppTheme.TEXT_DARK);
            defaultEchoChar = txtPassword.getEchoChar();

            btnEye = new JButton() {
                private static final long serialVersionUID = 1L;
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth();
                    int h = getHeight();
                    g2.setColor(passwordVisible ? AppTheme.HEADER : new Color(0x9E9BB5));
                    g2.setStroke(new BasicStroke(1.6f));

                    // Gambar ikon mata vektor presisi
                    int cx = w / 2;
                    int cy = h / 2;

                    // Lengkungan luar mata
                    java.awt.geom.Path2D.Double eyePath = new java.awt.geom.Path2D.Double();
                    eyePath.moveTo(cx - 8, cy);
                    eyePath.quadTo(cx, cy - 6, cx + 8, cy);
                    eyePath.quadTo(cx, cy + 6, cx - 8, cy);
                    g2.draw(eyePath);

                    // Pupil mata di tengah
                    g2.fillOval(cx - 2, cy - 2, 5, 5);

                    // Garis coret diagonal bila password disembunyikan
                    if (!passwordVisible) {
                        g2.drawLine(cx - 7, cy - 5, cx + 7, cy + 5);
                    }
                    g2.dispose();
                }
            };
            btnEye.setPreferredSize(new Dimension(36, 36));
            btnEye.setOpaque(false);
            btnEye.setContentAreaFilled(false);
            btnEye.setBorderPainted(false);
            btnEye.setFocusPainted(false);
            btnEye.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnEye.setToolTipText("Lihat Password");

            btnEye.addActionListener(e -> {
                passwordVisible = !passwordVisible;
                txtPassword.setEchoChar(passwordVisible ? (char) 0 : defaultEchoChar);
                btnEye.setToolTipText(passwordVisible ? "Sembunyikan Password" : "Lihat Password");
                btnEye.repaint();
                txtPassword.requestFocusInWindow();
            });

            txtPassword.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override public void focusGained(java.awt.event.FocusEvent e) { focused = true; repaint(); }
                @Override public void focusLost(java.awt.event.FocusEvent e) { focused = false; repaint(); }
            });

            add(txtPassword, BorderLayout.CENTER);
            add(btnEye,      BorderLayout.EAST);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.setColor(focused ? AppTheme.FOCUS_BORDER : AppTheme.BORDER_SUBTLE);
            g2.setStroke(new BasicStroke(focused ? 2f : 1.5f));
            g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }

        public char[] getPassword() { return txtPassword.getPassword(); }
        public void setText(String t) { txtPassword.setText(t); }
        public void selectAll() { txtPassword.selectAll(); }
        @Override public boolean requestFocusInWindow() { return txtPassword.requestFocusInWindow(); }
        public void addActionListener(java.awt.event.ActionListener l) { txtPassword.addActionListener(l); }
    }
}
