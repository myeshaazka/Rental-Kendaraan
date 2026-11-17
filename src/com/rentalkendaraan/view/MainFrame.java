package com.rentalkendaraan.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.rentalkendaraan.controller.RentalController;

/**
 * Jendela utama aplikasi dengan navigasi top-bar modern.
 *
 * <p>Menggunakan {@link CardLayout} untuk berpindah antar halaman
 * dan top navigation bar bergaya web-app modern sebagai pengganti JMenuBar.</p>
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    public static final String CARD_LOGIN     = "CARD_LOGIN";
    public static final String CARD_DASHBOARD = "CARD_DASHBOARD";
    public static final String CARD_KENDARAAN = "CARD_KENDARAAN";
    public static final String CARD_TRANSAKSI = "CARD_TRANSAKSI";

    private static final String JUDUL_APLIKASI = "Sistem Manajemen Rental Kendaraan";

    private final RentalController controller;
    private final CardLayout cardLayout  = new CardLayout();
    private final JPanel    panelKonten  = new JPanel(cardLayout);

    private final LoginPanel      loginPanel;
    private final DashboardPanel  dashboardPanel;
    private final KendaraanPanel  kendaraanPanel;
    private final TransaksiPanel  transaksiPanel;

    // Nav items
    private NavItem navDashboard;
    private NavItem navKendaraan;
    private NavItem navTransaksi;
    private NavItem navKeluar;
    private JPanel  topNav;
    private AppTheme.PulseDot pulseDot;

    private boolean sudahLogin = false;

    public MainFrame(RentalController controller) {
        super(JUDUL_APLIKASI);
        this.controller = controller;

        this.loginPanel      = new LoginPanel(this, controller);
        this.dashboardPanel  = new DashboardPanel(this, controller);
        this.kendaraanPanel  = new KendaraanPanel(this, controller);
        this.transaksiPanel  = new TransaksiPanel(this, controller);

        siapkanJendela();
        siapkanLayout();
    }

    // ---------------------------------------------------------------
    private void siapkanJendela() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        java.awt.Rectangle area = java.awt.GraphicsEnvironment
                .getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int lebar  = Math.min(1280, area.width  - 20);
        int tinggi = Math.min(800,  area.height - 20);
        setSize(Math.max(lebar, 900), Math.max(tinggi, 620));
        setMinimumSize(new Dimension(Math.min(1000, area.width - 20),
                Math.min(640, area.height - 20)));
        setLocationRelativeTo(null);
        getContentPane().setBackground(AppTheme.BG_MAIN);
    }

    private void siapkanLayout() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.BG_MAIN);

        // Top Navigation Bar
        topNav = buatTopNav();
        root.add(topNav, BorderLayout.NORTH);

        // Konten kartu
        panelKonten.setBackground(AppTheme.BG_MAIN);
        panelKonten.add(loginPanel,     CARD_LOGIN);
        panelKonten.add(dashboardPanel, CARD_DASHBOARD);
        panelKonten.add(kendaraanPanel, CARD_KENDARAAN);
        panelKonten.add(transaksiPanel, CARD_TRANSAKSI);
        root.add(panelKonten, BorderLayout.CENTER);

        setContentPane(root);

        // Status awal
        setNavEnabled(false);
        cardLayout.show(panelKonten, CARD_LOGIN);
        topNav.setVisible(false);
    }

    // ---------------------------------------------------------------
    // TOP NAVIGATION BAR
    // ---------------------------------------------------------------
    private JPanel buatTopNav() {
        AppTheme.GradientHeader nav = new AppTheme.GradientHeader(
                AppTheme.HEADER, new Color(0x9C8FFF));
        nav.setLayout(new BorderLayout());
        nav.setPreferredSize(new Dimension(0, 56));

        // ---- Kiri: logo badge vektor + judul brand terselaraskan ----
        JPanel panelKiri = new JPanel(new GridBagLayout());
        panelKiri.setOpaque(false);
        panelKiri.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 10));

        // Badge logo melengkung putih bersih dengan vektor mobil modern warna lavender
        AppTheme.CarBrandBadge logoBadge = new AppTheme.CarBrandBadge(
                32, Color.WHITE, AppTheme.HEADER);

        JLabel lblJudul = new JLabel("Rental Kendaraan");
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblJudul.setForeground(Color.WHITE);

        GridBagConstraints gbcKiri = new GridBagConstraints();
        gbcKiri.gridy = 0;
        gbcKiri.anchor = GridBagConstraints.CENTER;
        gbcKiri.insets = new Insets(0, 0, 0, 10);
        panelKiri.add(logoBadge, gbcKiri);

        gbcKiri.insets = new Insets(0, 0, 0, 0);
        panelKiri.add(lblJudul, gbcKiri);

        // ---- Tengah: nav items ----
        JPanel panelNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        panelNav.setOpaque(false);

        navDashboard = new NavItem("Dashboard");
        navKendaraan = new NavItem("Data Kendaraan");
        navTransaksi = new NavItem("Transaksi Sewa");

        navDashboard.addActionListener(e -> { if (pastikanLogin()) tampilkanDashboard(); });
        navKendaraan.addActionListener(e -> { if (pastikanLogin()) tampilkanKendaraan(); });
        navTransaksi.addActionListener(e -> { if (pastikanLogin()) tampilkanTransaksi(); });

        panelNav.add(navDashboard);
        panelNav.add(navKendaraan);
        panelNav.add(navTransaksi);

        // ---- Kanan: pulse dot + keluar ----
        JPanel panelKanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        panelKanan.setOpaque(false);

        pulseDot = new AppTheme.PulseDot();
        JLabel lblKoneksi = new JLabel("Terhubung ke Supabase Live");
        lblKoneksi.setFont(AppTheme.FONT_SMALL);
        lblKoneksi.setForeground(new Color(0xE0F7ED));

        navKeluar = new NavItem("Keluar");
        navKeluar.addActionListener(e -> keluarAplikasi());

        panelKanan.add(pulseDot);
        panelKanan.add(lblKoneksi);
        panelKanan.add(navKeluar);

        nav.add(panelKiri,  BorderLayout.WEST);
        nav.add(panelNav,   BorderLayout.CENTER);
        nav.add(panelKanan, BorderLayout.EAST);

        // Pastikan panel itu sendiri bisa menerima layout
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(nav, BorderLayout.CENTER);
        return wrapper;
    }

    private void setNavEnabled(boolean enabled) {
        if (navDashboard != null) navDashboard.setEnabled(enabled);
        if (navKendaraan != null) navKendaraan.setEnabled(enabled);
        if (navTransaksi != null) navTransaksi.setEnabled(enabled);
    }

    private void highlightNav(NavItem active) {
        for (NavItem item : new NavItem[]{navDashboard, navKendaraan, navTransaksi}) {
            if (item != null) item.setActive(item == active);
        }
    }

    // ---------------------------------------------------------------
    // NAVIGASI
    // ---------------------------------------------------------------
    private boolean pastikanLogin() {
        if (!sudahLogin) {
            UiHelper.tampilkanInfo(this, "Silakan login terlebih dahulu.");
            cardLayout.show(panelKonten, CARD_LOGIN);
            return false;
        }
        return true;
    }

    /** Dipanggil oleh {@link LoginPanel} setelah kredensial valid. */
    public void loginBerhasil() {
        sudahLogin = true;
        setNavEnabled(true);
        topNav.setVisible(true);
        tampilkanDashboard();
    }

    public void tampilkanDashboard() {
        if (!pastikanLogin()) return;
        dashboardPanel.muatData();
        cardLayout.show(panelKonten, CARD_DASHBOARD);
        highlightNav(navDashboard);
        setTitle(JUDUL_APLIKASI + " - Dashboard");
    }

    public void tampilkanKendaraan() {
        if (!pastikanLogin()) return;
        kendaraanPanel.muatData();
        cardLayout.show(panelKonten, CARD_KENDARAAN);
        highlightNav(navKendaraan);
        setTitle(JUDUL_APLIKASI + " - Manajemen Kendaraan");
    }

    public void tampilkanTransaksi() {
        if (!pastikanLogin()) return;
        transaksiPanel.muatData();
        cardLayout.show(panelKonten, CARD_TRANSAKSI);
        highlightNav(navTransaksi);
        setTitle(JUDUL_APLIKASI + " - Transaksi Sewa");
    }

    public void logout() {
        if (!UiHelper.konfirmasi(this, "Konfirmasi Logout", "Yakin ingin logout dari aplikasi?")) {
            return;
        }
        sudahLogin = false;
        setNavEnabled(false);
        topNav.setVisible(false);
        loginPanel.resetForm();
        cardLayout.show(panelKonten, CARD_LOGIN);
        setTitle(JUDUL_APLIKASI);
    }

    private void keluarAplikasi() {
        if (UiHelper.konfirmasi(this, "Konfirmasi Keluar", "Keluar dari aplikasi sekarang?")) {
            if (pulseDot != null) pulseDot.stop();
            dispose();
            System.exit(0);
        }
    }

    public RentalController getController() { return controller; }

    // ---------------------------------------------------------------
    // INNER CLASS: Nav Item Button
    // ---------------------------------------------------------------
    private static class NavItem extends javax.swing.JButton {
        private static final long serialVersionUID = 1L;
        private boolean active = false;
        private boolean hovered = false;

        NavItem(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(new Color(255, 255, 255, 200));
            setOpaque(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(140, 56));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            });
        }

        void setActive(boolean active) { this.active = active; repaint(); }
        boolean isActive() { return active; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            String text = getText();
            Font font = active ? new Font("Segoe UI", Font.BOLD, 13) : new Font("Segoe UI", Font.PLAIN, 13);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics(font);

            int textW = fm.stringWidth(text);

            // Background pill diposisikan tepat di tengah tombol (horizontal & vertikal)
            int pillH = 34;
            int pillY = (getHeight() - pillH) / 2;
            int pillW = Math.max(textW + 28, getWidth() - 14);
            int pillX = (getWidth() - pillW) / 2;

            if (active) {
                // Background putih transparan halus yang pas melingkupi teks
                g2.setColor(new Color(255, 255, 255, 45));
                g2.fillRoundRect(pillX, pillY, pillW, pillH, 10, 10);
                g2.setColor(new Color(255, 255, 255, 75));
                g2.setStroke(new java.awt.BasicStroke(1.2f));
                g2.drawRoundRect(pillX, pillY, pillW - 1, pillH - 1, 10, 10);
            } else if (hovered && isEnabled()) {
                g2.setColor(new Color(255, 255, 255, 22));
                g2.fillRoundRect(pillX, pillY, pillW, pillH, 10, 10);
            }

            // Teks tepat di tengah (horizontal & vertikal) di dalam pill
            int tx = (getWidth() - textW) / 2;
            int ty = pillY + (pillH + fm.getAscent() - fm.getDescent()) / 2;

            if (active) {
                g2.setColor(Color.WHITE);
            } else if (!isEnabled()) {
                g2.setColor(new Color(255, 255, 255, 80));
            } else {
                g2.setColor(hovered ? Color.WHITE : new Color(255, 255, 255, 200));
            }
            g2.drawString(text, tx, ty);
            g2.dispose();
        }
    }
}
