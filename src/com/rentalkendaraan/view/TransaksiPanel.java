package com.rentalkendaraan.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import com.rentalkendaraan.controller.RentalController;
import com.rentalkendaraan.exception.DataTidakDitemukanException;
import com.rentalkendaraan.exception.InputTidakValidException;
import com.rentalkendaraan.exception.KendaraanTidakTersediaException;
import com.rentalkendaraan.model.Kendaraan;
import com.rentalkendaraan.model.TransaksiSewa;

/**
 * Halaman Manajemen Penyewa & Transaksi Sewa dengan desain modern.
 *
 * <p>Menyediakan form transaksi baru, tabel riwayat transaksi, proses
 * pengembalian kendaraan, dan penghapusan riwayat transaksi.</p>
 */
public class TransaksiPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final MainFrame frame;
    private final RentalController controller;

    // Form fields
    private final JComboBox<Kendaraan>     cmbKendaraan = new JComboBox<>();
    private final AppTheme.ModernTextField txtNama      = new AppTheme.ModernTextField();
    private final AppTheme.ModernTextField txtKtp       = new AppTheme.ModernTextField();
    private final AppTheme.ModernTextField txtTelepon   = new AppTheme.ModernTextField();
    private final AppTheme.ModernTextField txtAlamat    = new AppTheme.ModernTextField();
    private final JSpinner spnLama = new JSpinner(new SpinnerNumberModel(1, 1, 365, 1));

    private final JRadioButton rbTunai    = new JRadioButton("Tunai", true);
    private final JRadioButton rbTransfer = new JRadioButton("Transfer Bank");
    private final JRadioButton rbKartu    = new JRadioButton("Kartu Kredit");
    private final JCheckBox    chkAsuransi = new JCheckBox("Asuransi tambahan (+Rp25.000/hari)");

    private final JLabel lblEstimasi = new JLabel("-");

    // Tabel
    private final TransaksiTableModel modelTabel = new TransaksiTableModel();
    private final JTable              tabel      = new JTable(modelTabel);

    // Scroll pane form
    private JScrollPane scrollForm;

    // Tombol
    private final AppTheme.ModernButton btnSewa =
            new AppTheme.ModernButton("💾 Simpan Transaksi", AppTheme.HEADER, AppTheme.HEADER_HOVER);
    private final AppTheme.ModernButton btnKembali =
            new AppTheme.ModernButton("✔ Proses Pengembalian", AppTheme.SAGE_GREEN, AppTheme.SAGE_GREEN.darker());
    private final AppTheme.ModernButton btnHapus =
            new AppTheme.ModernButton("🗑 Hapus", AppTheme.CORAL, AppTheme.CORAL.darker());
    private final AppTheme.GhostButton  btnRefresh =
            new AppTheme.GhostButton("↻ Muat Ulang", AppTheme.HEADER);
    private final AppTheme.GhostButton  btnBersih =
            new AppTheme.GhostButton("Bersihkan", AppTheme.TEXT_MUTED);

    private final JLabel lblStatus = new JLabel(" ");

    private List<TransaksiSewa> semuaTransaksi;

    public TransaksiPanel(MainFrame frame, RentalController controller) {
        this.frame      = frame;
        this.controller = controller;

        setLayout(new BorderLayout(0, 14));
        setBackground(AppTheme.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        add(buatHeader(),      BorderLayout.NORTH);
        add(buatKonten(),      BorderLayout.CENTER);
        add(buatBarisTombol(), BorderLayout.SOUTH);

        AppTheme.styleComboBox(cmbKendaraan);
        AppTheme.styleSpinner(spnLama);
        styleRadio(rbTunai); styleRadio(rbTransfer); styleRadio(rbKartu);
        styleCheckBox(chkAsuransi);

        pasangAksi();
        perbaruiTombol();
    }

    // ---------------------------------------------------------------
    // HEADER
    // ---------------------------------------------------------------
    private JPanel buatHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel panelTeks = new JPanel(new BorderLayout(0, 3));
        panelTeks.setOpaque(false);

        JLabel lblJudul = new JLabel("Penyewa & Transaksi Sewa");
        lblJudul.setFont(AppTheme.FONT_TITLE);
        lblJudul.setForeground(AppTheme.TEXT_DARK);

        JLabel lblSub = new JLabel("Catat penyewa baru, proses sewa, pengembalian, dan riwayat transaksi");
        lblSub.setFont(AppTheme.FONT_BODY);
        lblSub.setForeground(AppTheme.TEXT_MUTED);

        panelTeks.add(lblJudul, BorderLayout.NORTH);
        panelTeks.add(lblSub,  BorderLayout.SOUTH);

        lblStatus.setFont(AppTheme.FONT_SMALL);
        lblStatus.setForeground(AppTheme.TEXT_MUTED);

        JPanel panelKanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelKanan.setOpaque(false);
        panelKanan.add(lblStatus);

        header.add(panelTeks,  BorderLayout.WEST);
        header.add(panelKanan, BorderLayout.EAST);
        return header;
    }

    // ---------------------------------------------------------------
    // KONTEN (form kiri + tabel kanan)
    // ---------------------------------------------------------------
    private JPanel buatKonten() {
        JPanel konten = new JPanel(new BorderLayout(16, 0));
        konten.setOpaque(false);

        // Form card (dilebarkan ke 510px agar teks combo box tidak terpotong)
        AppTheme.CardPanel cardForm = new AppTheme.CardPanel(16, 5);
        cardForm.setLayout(new BorderLayout());
        cardForm.setPreferredSize(new Dimension(510, 0));

        scrollForm = new JScrollPane(buatFormInner(),
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollForm.setBorder(BorderFactory.createEmptyBorder());
        scrollForm.setOpaque(false);
        scrollForm.getViewport().setOpaque(false);
        scrollForm.getVerticalScrollBar().setUnitIncrement(16);
        scrollForm.getHorizontalScrollBar().setUnitIncrement(16);
        cardForm.add(scrollForm, BorderLayout.CENTER);

        // Tabel card
        AppTheme.CardPanel cardTabel = new AppTheme.CardPanel(16, 5);
        cardTabel.setLayout(new BorderLayout());
        cardTabel.add(buatPanelTabelInner(), BorderLayout.CENTER);

        konten.add(cardForm,  BorderLayout.WEST);
        konten.add(cardTabel, BorderLayout.CENTER);
        return konten;
    }

    private JPanel buatFormInner() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Judul form
        JLabel lblTitle = new JLabel("Form Transaksi Sewa");
        lblTitle.setFont(AppTheme.FONT_HEADING);
        lblTitle.setForeground(AppTheme.TEXT_DARK);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        form.add(lblTitle, gbc);

        int baris = 1;
        baris = tambahField(form, gbc, baris, "KENDARAAN TERSEDIA", cmbKendaraan);
        baris = tambahField(form, gbc, baris, "NAMA PENYEWA", txtNama);
        baris = tambahField(form, gbc, baris, "NO. KTP (16 DIGIT)", txtKtp);
        baris = tambahField(form, gbc, baris, "NO. TELEPON", txtTelepon);
        baris = tambahField(form, gbc, baris, "ALAMAT", txtAlamat);
        baris = tambahField(form, gbc, baris, "LAMA SEWA (HARI)", spnLama);

        // Metode pembayaran (leluasa 1 baris)
        gbc.gridy = baris;
        gbc.insets = new Insets(6, 0, 2, 0);
        form.add(AppTheme.makeFormLabel("METODE PEMBAYARAN"), gbc);

        JPanel panelMetode = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelMetode.setOpaque(false);
        ButtonGroup grup = new ButtonGroup();
        grup.add(rbTunai); grup.add(rbTransfer); grup.add(rbKartu);
        panelMetode.add(rbTunai); panelMetode.add(rbTransfer); panelMetode.add(rbKartu);

        gbc.gridy = baris + 1;
        gbc.insets = new Insets(0, 0, 6, 0);
        form.add(panelMetode, gbc);
        baris += 2;

        // Asuransi (diberikan margin aman agar kotak centang tidak terpotong)
        chkAsuransi.setMargin(new Insets(2, 4, 2, 4));
        gbc.gridy = baris;
        gbc.insets = new Insets(4, 2, 8, 0);
        form.add(chkAsuransi, gbc);
        baris++;

        // Estimasi biaya (horizontal card dengan 2 baris agar angka rupiah tidak terpotong)
        JPanel estimasiCard = buatEstimasiCard();
        gbc.gridy = baris;
        gbc.insets = new Insets(2, 0, 10, 0);
        form.add(estimasiCard, gbc);
        baris++;

        // Tombol Simpan Transaksi langsung di bawah pengisian form
        btnSewa.setPreferredSize(new Dimension(0, 40));
        gbc.gridy = baris;
        gbc.insets = new Insets(4, 0, 4, 0);
        form.add(btnSewa, gbc);

        return form;
    }

    private JPanel buatEstimasiCard() {
        JPanel card = new JPanel(new BorderLayout(0, 2));
        card.setBackground(new Color(0xF0EEFF));
        card.setBorder(BorderFactory.createCompoundBorder(
                new AppTheme.RoundedBorder(10, AppTheme.BORDER_SUBTLE, 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        card.setOpaque(true);

        JLabel lblTitle = new JLabel("Estimasi Biaya Sewa:");
        lblTitle.setFont(AppTheme.FONT_SMALL);
        lblTitle.setForeground(AppTheme.TEXT_MUTED);

        lblEstimasi.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblEstimasi.setForeground(AppTheme.HEADER);

        card.add(lblTitle,    BorderLayout.NORTH);
        card.add(lblEstimasi, BorderLayout.CENTER);
        return card;
    }

    private int tambahField(JPanel panel, GridBagConstraints gbc, int baris,
            String label, JComponent komponen) {
        gbc.gridy = baris;
        gbc.insets = new Insets(6, 0, 2, 0);
        panel.add(AppTheme.makeFormLabel(label), gbc);

        gbc.gridy = baris + 1;
        gbc.insets = new Insets(0, 0, 6, 0);
        panel.add(komponen, gbc);
        return baris + 2;
    }

    private JPanel buatPanelTabelInner() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel lblTitle = new JLabel("Daftar Transaksi Sewa");
        lblTitle.setFont(AppTheme.FONT_HEADING);
        lblTitle.setForeground(AppTheme.TEXT_DARK);

        AppTheme.styleTable(tabel);
        // Aktifkan mode geser horizontal agar kolom tidak terpotong (AUTO_RESIZE_OFF)
        tabel.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] lebarKolom = {55, 250, 140, 65, 165, 130, 95, 115, 75};
        for (int i = 0; i < lebarKolom.length && i < tabel.getColumnCount(); i++) {
            tabel.getColumnModel().getColumn(i).setPreferredWidth(lebarKolom[i]);
        }
        UiHelper.pasangTooltipTabel(tabel);

        // Center renderer bergaris zebra untuk kolom ID (0), Hari (3), dan Asuransi (8)
        javax.swing.table.DefaultTableCellRenderer centerZebra =
                new javax.swing.table.DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(AppTheme.FONT_BODY);
                if (sel) {
                    setBackground(AppTheme.ROW_SELECTED);
                    setForeground(AppTheme.TEXT_DARK);
                } else {
                    setBackground(row % 2 == 0 ? AppTheme.CARD_BG : AppTheme.ROW_ALT);
                    setForeground(AppTheme.TEXT_DARK);
                }
                return this;
            }
        };
        tabel.getColumnModel().getColumn(0).setCellRenderer(centerZebra);
        tabel.getColumnModel().getColumn(3).setCellRenderer(centerZebra);
        tabel.getColumnModel().getColumn(8).setCellRenderer(centerZebra);

        // Status renderer untuk kolom Status (index 6)
        tabel.getColumnModel().getColumn(6).setCellRenderer(
                new TransaksiStatusRenderer());

        JScrollPane scroll = AppTheme.makeScrollPane(tabel);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(scroll,   BorderLayout.CENTER);
        return panel;
    }

    // ---------------------------------------------------------------
    // BARIS TOMBOL
    // ---------------------------------------------------------------
    private JPanel buatBarisTombol() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        panel.setOpaque(false);

        panel.add(btnBersih);
        panel.add(btnRefresh);
        panel.add(btnHapus);
        panel.add(btnKembali);
        return panel;
    }

    // ---------------------------------------------------------------
    // AKSI
    // ---------------------------------------------------------------
    private void pasangAksi() {
        cmbKendaraan.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int idx, boolean sel, boolean foc) {
                super.getListCellRendererComponent(list, value, idx, sel, foc);
                if (value instanceof Kendaraan) {
                    Kendaraan k = (Kendaraan) value;
                    String tarif = String.format("Rp %,.0f/hari", k.getTarifHarian()).replace(',', '.');
                    String itemText = k.getKodeKendaraan() + " | " + k.getMerk()
                            + " (" + k.getPlatNomor() + ") - " + tarif;
                    setText(itemText);
                    setToolTipText(itemText);
                }
                return this;
            }
        });

        cmbKendaraan.addActionListener(e -> perbaruiEstimasi());
        spnLama.addChangeListener(e -> perbaruiEstimasi());
        chkAsuransi.addActionListener(e -> perbaruiEstimasi());

        btnSewa.addActionListener(e -> simpanTransaksi());
        btnKembali.addActionListener(e -> prosesPengembalian());
        btnHapus.addActionListener(e -> hapusTransaksi());
        btnBersih.addActionListener(e -> bersihkanForm());
        btnRefresh.addActionListener(e -> muatData());

        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) perbaruiTombol();
        });
    }

    private void simpanTransaksi() {
        Kendaraan kendaraan = (Kendaraan) cmbKendaraan.getSelectedItem();
        if (kendaraan == null) {
            UiHelper.tampilkanError(this,
                    "Belum ada kendaraan yang tersedia. Tambahkan atau kembalikan "
                    + "kendaraan terlebih dahulu pada halaman Manajemen Kendaraan.");
            return;
        }
        try {
            controller.buatTransaksi(
                    kendaraan.getKodeKendaraan(), txtNama.getText(), txtKtp.getText(),
                    txtTelepon.getText(), txtAlamat.getText(), ambilLamaSewa(),
                    getMetodePembayaran(), chkAsuransi.isSelected());
            UiHelper.tampilkanInfo(this,
                    "Transaksi sewa berhasil disimpan.\n"
                    + "Status kendaraan " + kendaraan.getKodeKendaraan()
                    + " sekarang menjadi Disewa.");
            bersihkanForm();
            muatData();
        } catch (InputTidakValidException | KendaraanTidakTersediaException ex) {
            UiHelper.tampilkanError(this, ex.getMessage());
        } catch (DataTidakDitemukanException ex) {
            UiHelper.tampilkanError(this, ex.getMessage());
        } catch (SQLException ex) {
            UiHelper.tampilkanError(this, "Gagal menyimpan transaksi:\n" + ex.getMessage());
        }
    }

    private void prosesPengembalian() {
        TransaksiSewa transaksi = ambilTransaksiTerpilih();
        if (transaksi == null) {
            UiHelper.tampilkanError(this, "Pilih transaksi yang akan diproses pengembaliannya.");
            return;
        }
        if (!transaksi.isTersedia()) {
            UiHelper.tampilkanError(this, "Transaksi ini sudah selesai; kendaraan sudah dikembalikan.");
            return;
        }
        int pilihan = JOptionPane.showConfirmDialog(this,
                "Proses pengembalian kendaraan "
                + transaksi.getKendaraan().getKodeKendaraan() + " - "
                + transaksi.getKendaraan().getMerk() + "?\n"
                + "Status transaksi akan menjadi SELESAI dan kendaraan kembali Tersedia.",
                "Konfirmasi Pengembalian",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (pilihan != JOptionPane.YES_OPTION) return;
        try {
            controller.kembalikanKendaraan(transaksi.getIdTransaksi());
            UiHelper.tampilkanInfo(this, "Pengembalian berhasil diproses. Kendaraan kembali tersedia.");
            muatData();
        } catch (InputTidakValidException | DataTidakDitemukanException ex) {
            UiHelper.tampilkanError(this, ex.getMessage());
        } catch (SQLException ex) {
            UiHelper.tampilkanError(this, "Gagal memproses pengembalian:\n" + ex.getMessage());
        }
    }

    private void hapusTransaksi() {
        TransaksiSewa transaksi = ambilTransaksiTerpilih();
        if (transaksi == null) {
            UiHelper.tampilkanError(this, "Pilih transaksi yang akan dihapus.");
            return;
        }
        String catatan = transaksi.isTersedia()
                ? "\nTransaksi ini masih AKTIF; kendaraan akan otomatis dikembalikan ke status Tersedia."
                : "";
        int pilihan = JOptionPane.showConfirmDialog(this,
                "Hapus riwayat transaksi TRX-" + transaksi.getIdTransaksi()
                + " (" + transaksi.getPenyewa().getNama() + ")?"
                + catatan + "\nData yang dihapus tidak dapat dikembalikan.",
                "Konfirmasi Hapus Transaksi",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (pilihan != JOptionPane.YES_OPTION) return;
        try {
            controller.hapusTransaksi(transaksi.getIdTransaksi());
            UiHelper.tampilkanInfo(this, "Riwayat transaksi berhasil dihapus.");
            muatData();
        } catch (InputTidakValidException ex) {
            UiHelper.tampilkanError(this, ex.getMessage());
        } catch (SQLException ex) {
            UiHelper.tampilkanError(this, "Gagal menghapus transaksi:\n" + ex.getMessage());
        }
    }

    private void bersihkanForm() {
        txtNama.setText(""); txtKtp.setText(""); txtTelepon.setText(""); txtAlamat.setText("");
        spnLama.setValue(1);
        rbTunai.setSelected(true);
        chkAsuransi.setSelected(false);
        tabel.clearSelection();
        if (cmbKendaraan.getItemCount() > 0) cmbKendaraan.setSelectedIndex(0);
        perbaruiEstimasi();
        perbaruiTombol();
        if (scrollForm != null) {
            javax.swing.SwingUtilities.invokeLater(() ->
                    scrollForm.getViewport().setViewPosition(new java.awt.Point(0, 0)));
        }
    }

    private void perbaruiTombol() {
        TransaksiSewa t = ambilTransaksiTerpilih();
        btnHapus.setEnabled(t != null);
        btnKembali.setEnabled(t != null && t.isTersedia());
    }

    private TransaksiSewa ambilTransaksiTerpilih() {
        int barisView = tabel.getSelectedRow();
        if (barisView < 0) return null;
        return modelTabel.getTransaksi(tabel.convertRowIndexToModel(barisView));
    }

    private void perbaruiEstimasi() {
        Kendaraan k = (Kendaraan) cmbKendaraan.getSelectedItem();
        if (k == null) {
            lblEstimasi.setText("-");
            cmbKendaraan.setToolTipText(null);
            return;
        }
        String tarif = String.format("Rp %,.0f/hari", k.getTarifHarian()).replace(',', '.');
        cmbKendaraan.setToolTipText(k.getKodeKendaraan() + " | " + k.getMerk()
                + " (" + k.getPlatNomor() + ") - " + tarif);
        try {
            double total = controller.hitungEstimasi(k, ambilLamaSewa(), chkAsuransi.isSelected());
            lblEstimasi.setText(UiHelper.rupiah(total));
        } catch (InputTidakValidException ex) {
            lblEstimasi.setText("-");
        }
    }

    private int ambilLamaSewa() {
        return ((Number) spnLama.getValue()).intValue();
    }

    private String getMetodePembayaran() {
        if (rbTransfer.isSelected()) return "Transfer Bank";
        if (rbKartu.isSelected())    return "Kartu Kredit";
        return "Tunai";
    }

    public void muatData() {
        try {
            semuaTransaksi = controller.ambilSemuaTransaksi();
            modelTabel.setData(semuaTransaksi);
            muatKendaraanTersedia();
            lblStatus.setText(semuaTransaksi.size() + " transaksi");
            tabel.clearSelection();
            perbaruiTombol();
            if (scrollForm != null) {
                javax.swing.SwingUtilities.invokeLater(() ->
                        scrollForm.getViewport().setViewPosition(new java.awt.Point(0, 0)));
            }
        } catch (SQLException ex) {
            UiHelper.tampilkanError(this,
                    "Gagal memuat data transaksi dari database:\n" + ex.getMessage());
        }
    }

    private void muatKendaraanTersedia() throws SQLException {
        Kendaraan sebelumnya = (Kendaraan) cmbKendaraan.getSelectedItem();
        DefaultComboBoxModel<Kendaraan> model = new DefaultComboBoxModel<>();
        for (Kendaraan k : controller.ambilKendaraanTersedia()) model.addElement(k);
        cmbKendaraan.setModel(model);

        if (sebelumnya != null) {
            for (int i = 0; i < model.getSize(); i++) {
                if (model.getElementAt(i).getKodeKendaraan()
                        .equals(sebelumnya.getKodeKendaraan())) {
                    cmbKendaraan.setSelectedIndex(i);
                    break;
                }
            }
        }
        cmbKendaraan.setEnabled(model.getSize() > 0);
        perbaruiEstimasi();
    }

    private static void styleRadio(JRadioButton rb) {
        rb.setFont(AppTheme.FONT_BODY);
        rb.setForeground(AppTheme.TEXT_DARK);
        rb.setOpaque(false);
        rb.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }

    private static void styleCheckBox(JCheckBox cb) {
        cb.setFont(AppTheme.FONT_BODY);
        cb.setForeground(AppTheme.TEXT_DARK);
        cb.setOpaque(false);
        cb.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }

    // ---------------------------------------------------------------
    // STATUS CELL RENDERER Transaksi
    // ---------------------------------------------------------------
    private static class TransaksiStatusRenderer
            extends javax.swing.table.DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean selected, boolean focus, int row, int col) {
            String text = value == null ? "" : value.toString();
            boolean aktif = text.equalsIgnoreCase("Aktif") || text.equalsIgnoreCase("Disewa");

            AppTheme.StatusBadge badge = new AppTheme.StatusBadge(
                    text, aktif ? AppTheme.AMBER : AppTheme.SAGE_GREEN);

            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
            wrapper.setBackground(selected ? AppTheme.ROW_SELECTED :
                    (row % 2 == 0 ? AppTheme.CARD_BG : AppTheme.ROW_ALT));
            wrapper.add(badge);
            return wrapper;
        }
    }
}
