package com.rentalkendaraan.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import com.rentalkendaraan.controller.RentalController;
import com.rentalkendaraan.model.StatistikDashboard;
import com.rentalkendaraan.model.TransaksiSewa;

/**
 * Halaman Dashboard: ringkasan armada, status sewa, pendapatan,
 * dan daftar kendaraan aktif disewa — dengan animated counter dan
 * card shadow estetik.
 */
public class DashboardPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMAT_JAM =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final NumberFormat FORMAT_RUPIAH =
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));

    private final MainFrame frame;
    private final RentalController controller;

    // Animated counters
    private final AppTheme.AnimatedCounter cntTotal     = makeCounter();
    private final AppTheme.AnimatedCounter cntTersedia  = makeCounter();
    private final AppTheme.AnimatedCounter cntDisewa    = makeCounter();
    private final AppTheme.AnimatedCounter cntPendapatan;
    private final AppTheme.AnimatedCounter cntTransaksi = makeCounter();

    private final JLabel lblDiperbarui = new JLabel(" ");
    private final TransaksiTableModel modelTabel = new TransaksiTableModel();
    private final JTable tabelAktif = new JTable(modelTabel);

    public DashboardPanel(MainFrame frame, RentalController controller) {
        this.frame      = frame;
        this.controller = controller;

        // Counter pendapatan dengan format rupiah
        this.cntPendapatan = new AppTheme.AnimatedCounter("", "", v -> FORMAT_RUPIAH.format(v));

        setLayout(new BorderLayout(0, 18));
        setBackground(AppTheme.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        add(buatHeader(),  BorderLayout.NORTH);
        add(buatIsi(),     BorderLayout.CENTER);
    }

    // ---------------------------------------------------------------
    private static AppTheme.AnimatedCounter makeCounter() {
        AppTheme.AnimatedCounter c = new AppTheme.AnimatedCounter("", "", v -> String.valueOf(v));
        return c;
    }

    // ---------------------------------------------------------------
    // HEADER
    // ---------------------------------------------------------------
    private JPanel buatHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        JPanel panelTeks = new JPanel(new BorderLayout(0, 3));
        panelTeks.setOpaque(false);

        JLabel lblJudul = new JLabel("Dashboard");
        lblJudul.setFont(AppTheme.FONT_TITLE);
        lblJudul.setForeground(AppTheme.TEXT_DARK);

        JLabel lblSub = new JLabel("Ringkasan armada, transaksi, dan pendapatan rental kendaraan");
        lblSub.setFont(AppTheme.FONT_BODY);
        lblSub.setForeground(AppTheme.TEXT_MUTED);

        panelTeks.add(lblJudul, BorderLayout.NORTH);
        panelTeks.add(lblSub,  BorderLayout.SOUTH);

        JPanel panelTombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelTombol.setOpaque(false);

        AppTheme.GhostButton btnKelola = new AppTheme.GhostButton("Kelola Transaksi", AppTheme.HEADER);
        btnKelola.addActionListener(e -> frame.tampilkanTransaksi());

        AppTheme.ModernButton btnRefresh = new AppTheme.ModernButton(
                "↻  Muat Ulang", AppTheme.HEADER, AppTheme.HEADER_HOVER);
        btnRefresh.addActionListener(e -> muatData());

        panelTombol.add(btnKelola);
        panelTombol.add(btnRefresh);

        header.add(panelTeks,   BorderLayout.WEST);
        header.add(panelTombol, BorderLayout.EAST);
        return header;
    }

    // ---------------------------------------------------------------
    // KONTEN UTAMA
    // ---------------------------------------------------------------
    private JPanel buatIsi() {
        JPanel isi = new JPanel(new BorderLayout(0, 16));
        isi.setOpaque(false);

        // Stat cards
        JPanel panelKartu = new JPanel(new GridLayout(1, 5, 14, 0));
        panelKartu.setOpaque(false);

        panelKartu.add(buatStatCard("Total Armada",    cntTotal,      AppTheme.HEADER,      "🚘"));
        panelKartu.add(buatStatCard("Tersedia",        cntTersedia,   AppTheme.SAGE_GREEN,  "✅"));
        panelKartu.add(buatStatCard("Sedang Disewa",   cntDisewa,     AppTheme.AMBER,       "⏳"));
        panelKartu.add(buatStatCard("Total Pendapatan",cntPendapatan, new Color(0x7E5EF8),  "💰"));
        panelKartu.add(buatStatCard("Transaksi",       cntTransaksi,  new Color(0x2BBFCF),  "📋"));

        // Tabel aktif
        JPanel panelTabel = buatPanelTabel();

        isi.add(panelKartu,  BorderLayout.NORTH);
        isi.add(panelTabel,  BorderLayout.CENTER);
        return isi;
    }

    private JPanel buatStatCard(String judul, AppTheme.AnimatedCounter counter,
            Color accentColor, String emoji) {
        AppTheme.CardPanel kartu = new AppTheme.CardPanel(16, 6);
        kartu.setLayout(new BorderLayout());

        JPanel inner = new JPanel(new BorderLayout(0, 6));
        inner.setOpaque(false);
        inner.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        // Baris atas: judul + emoji
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel lblJudul = new JLabel(judul);
        lblJudul.setFont(AppTheme.FONT_SMALL);
        lblJudul.setForeground(AppTheme.TEXT_MUTED);

        JLabel lblEmoji = new JLabel(emoji);
        lblEmoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        lblEmoji.setHorizontalAlignment(SwingConstants.RIGHT);

        topRow.add(lblJudul, BorderLayout.WEST);
        topRow.add(lblEmoji, BorderLayout.EAST);

        // Nilai counter
        counter.setFont(new Font("Segoe UI", Font.BOLD, 24));
        counter.setForeground(accentColor);

        // Accent bar bawah
        JPanel bar = new JPanel();
        bar.setBackground(accentColor);
        bar.setPreferredSize(new Dimension(0, 3));
        bar.setOpaque(true);

        inner.add(topRow,   BorderLayout.NORTH);
        inner.add(counter,  BorderLayout.CENTER);

        kartu.add(inner, BorderLayout.CENTER);
        kartu.add(bar,   BorderLayout.SOUTH);
        return kartu;
    }

    private JPanel buatPanelTabel() {
        AppTheme.CardPanel kartu = new AppTheme.CardPanel(16, 6);
        kartu.setLayout(new BorderLayout());

        JPanel inner = new JPanel(new BorderLayout(0, 10));
        inner.setOpaque(false);
        inner.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // Judul section
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblJudul = new JLabel("Kendaraan Sedang Disewa");
        lblJudul.setFont(AppTheme.FONT_HEADING);
        lblJudul.setForeground(AppTheme.TEXT_DARK);

        lblDiperbarui.setFont(AppTheme.FONT_SMALL);
        lblDiperbarui.setForeground(AppTheme.TEXT_MUTED);
        lblDiperbarui.setHorizontalAlignment(SwingConstants.RIGHT);

        titleRow.add(lblJudul,      BorderLayout.WEST);
        titleRow.add(lblDiperbarui, BorderLayout.EAST);

        // Tabel
        AppTheme.styleTable(tabelAktif);
        tabelAktif.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] lebarKolom = {55, 240, 140, 65, 165, 130, 95, 115, 75};
        for (int i = 0; i < lebarKolom.length && i < tabelAktif.getColumnCount(); i++) {
            tabelAktif.getColumnModel().getColumn(i).setPreferredWidth(lebarKolom[i]);
        }
        UiHelper.pasangTooltipTabel(tabelAktif);

        JScrollPane scroll = AppTheme.makeScrollPane(tabelAktif);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        inner.add(titleRow, BorderLayout.NORTH);
        inner.add(scroll,   BorderLayout.CENTER);
        kartu.add(inner, BorderLayout.CENTER);
        return kartu;
    }

    // ---------------------------------------------------------------
    // MUAT DATA
    // ---------------------------------------------------------------
    public void muatData() {
        try {
            StatistikDashboard stat = controller.ambilStatistik();

            cntTotal.animateTo(stat.getTotalKendaraan());
            cntTersedia.animateTo(stat.getJumlahTersedia());
            cntDisewa.animateTo(stat.getJumlahDisewa());
            cntPendapatan.animateTo((long) stat.getTotalPendapatan());
            cntTransaksi.animateTo(stat.getTransaksiAktif() + stat.getTransaksiSelesai());

            List<TransaksiSewa> aktif = new ArrayList<>();
            for (TransaksiSewa t : controller.ambilSemuaTransaksi()) {
                if (t.isTersedia()) aktif.add(t);
            }
            modelTabel.setData(aktif);
            lblDiperbarui.setText("Diperbarui " + LocalTime.now().format(FORMAT_JAM));

        } catch (SQLException ex) {
            UiHelper.tampilkanError(this,
                    "Gagal memuat ringkasan dashboard dari database:\n" + ex.getMessage());
        }
    }
}
