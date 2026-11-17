package com.rentalkendaraan.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.rentalkendaraan.controller.RentalController;
import com.rentalkendaraan.exception.InputTidakValidException;
import com.rentalkendaraan.model.Bus;
import com.rentalkendaraan.model.Kendaraan;
import com.rentalkendaraan.model.Mobil;
import com.rentalkendaraan.model.Motor;

/**
 * Halaman Manajemen Data Kendaraan (CRUD penuh untuk Mobil, Motor, dan Bus)
 * dengan desain modern Pastel Lavender.
 */
public class KendaraanPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final MainFrame frame;
    private final RentalController controller;

    // Form fields
    private final AppTheme.ModernTextField txtKode   = new AppTheme.ModernTextField();
    private final JComboBox<String>        cmbJenis  =
            new JComboBox<>(new String[]{"Mobil", "Motor", "Bus"});
    private final AppTheme.ModernTextField txtMerk   = new AppTheme.ModernTextField();
    private final AppTheme.ModernTextField txtPlat   = new AppTheme.ModernTextField();
    private final AppTheme.ModernTextField txtTarif  = new AppTheme.ModernTextField();
    private final JSpinner spnKapasitas =
            new JSpinner(new SpinnerNumberModel(1000, 50, 200000, 50));
    private final JLabel   lblKapasitas = new JLabel("Kapasitas (khusus Motor/Bus)");
    private final JCheckBox chkSopir = new JCheckBox("Dengan Sopir (khusus Mobil)");

    // Search + tabel
    private final AppTheme.ModernTextField txtCari   = new AppTheme.ModernTextField();
    private final KendaraanTableModel      modelTabel = new KendaraanTableModel();
    private final JTable                   tabel      = new JTable(modelTabel);

    // Scroll pane form
    private JScrollPane scrollForm;

    // Tombol
    private final AppTheme.ModernButton btnTambah =
            new AppTheme.ModernButton("+ Tambah", AppTheme.HEADER, AppTheme.HEADER_HOVER);
    private final AppTheme.ModernButton btnUbah   =
            new AppTheme.ModernButton("✏ Ubah",   AppTheme.AMBER, AppTheme.AMBER.darker());
    private final AppTheme.ModernButton btnHapus  =
            new AppTheme.ModernButton("🗑 Hapus",  AppTheme.CORAL, AppTheme.CORAL.darker());
    private final AppTheme.GhostButton  btnBersih =
            new AppTheme.GhostButton("Bersihkan", AppTheme.TEXT_MUTED);
    private final AppTheme.GhostButton  btnRefresh =
            new AppTheme.GhostButton("↻ Muat Ulang", AppTheme.HEADER);

    private final JLabel lblStatus = new JLabel(" ");

    private List<Kendaraan> semuaKendaraan = new ArrayList<>();
    private String kodeTerpilih;
    private boolean tersediaTerpilih = true;

    public KendaraanPanel(MainFrame frame, RentalController controller) {
        this.frame      = frame;
        this.controller = controller;

        setLayout(new BorderLayout(0, 14));
        setBackground(AppTheme.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        add(buatHeader(),  BorderLayout.NORTH);
        add(buatKonten(),  BorderLayout.CENTER);
        add(buatBarisTombol(), BorderLayout.SOUTH);

        AppTheme.styleComboBox(cmbJenis);
        AppTheme.styleSpinner(spnKapasitas);
        styleCheckBox(chkSopir);

        pasangAksi();
        sesuaikanFormJenis();
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

        JLabel lblJudul = new JLabel("Manajemen Data Kendaraan");
        lblJudul.setFont(AppTheme.FONT_TITLE);
        lblJudul.setForeground(AppTheme.TEXT_DARK);

        JLabel lblSub = new JLabel("Tambah, lihat, ubah, dan hapus armada Mobil / Motor / Bus");
        lblSub.setFont(AppTheme.FONT_BODY);
        lblSub.setForeground(AppTheme.TEXT_MUTED);

        panelTeks.add(lblJudul, BorderLayout.NORTH);
        panelTeks.add(lblSub,  BorderLayout.SOUTH);

        lblStatus.setFont(AppTheme.FONT_SMALL);
        lblStatus.setForeground(AppTheme.TEXT_MUTED);

        JPanel panelKanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelKanan.setOpaque(false);
        panelKanan.add(lblStatus);

        header.add(panelTeks, BorderLayout.WEST);
        header.add(panelKanan, BorderLayout.EAST);
        return header;
    }

    // ---------------------------------------------------------------
    // KONTEN UTAMA (form kiri + tabel kanan)
    // ---------------------------------------------------------------
    private JPanel buatKonten() {
        JPanel konten = new JPanel(new BorderLayout(16, 0));
        konten.setOpaque(false);

        // Form dalam card (kembali ke layout 1 kolom luas agar tidak kepotong)
        AppTheme.CardPanel cardForm = new AppTheme.CardPanel(16, 5);
        cardForm.setLayout(new BorderLayout());
        cardForm.setPreferredSize(new Dimension(380, 0));

        JPanel formInner = buatFormInner();
        scrollForm = new JScrollPane(formInner,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollForm.setBorder(BorderFactory.createEmptyBorder());
        scrollForm.setOpaque(false);
        scrollForm.getViewport().setOpaque(false);
        scrollForm.getVerticalScrollBar().setUnitIncrement(16);

        cardForm.add(scrollForm, BorderLayout.CENTER);

        // Panel tabel dalam card
        AppTheme.CardPanel cardTabel = new AppTheme.CardPanel(16, 5);
        cardTabel.setLayout(new BorderLayout());

        JPanel tabelInner = buatPanelTabelInner();
        cardTabel.add(tabelInner, BorderLayout.CENTER);

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
        JLabel lblFormTitle = new JLabel("Form Kendaraan");
        lblFormTitle.setFont(AppTheme.FONT_HEADING);
        lblFormTitle.setForeground(AppTheme.TEXT_DARK);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        form.add(lblFormTitle, gbc);

        int baris = 1;
        baris = tambahField(form, gbc, baris, "KODE KENDARAAN", txtKode);
        baris = tambahField(form, gbc, baris, "JENIS KENDARAAN", cmbJenis);
        baris = tambahField(form, gbc, baris, "MERK", txtMerk);
        baris = tambahField(form, gbc, baris, "PLAT NOMOR", txtPlat);
        baris = tambahField(form, gbc, baris, "TARIF HARIAN (Rp)", txtTarif);

        // Kapasitas
        gbc.gridy = baris;
        gbc.insets = new Insets(6, 0, 2, 0);
        lblKapasitas.setFont(AppTheme.FONT_SMALL);
        lblKapasitas.setForeground(AppTheme.TEXT_MUTED);
        form.add(lblKapasitas, gbc);

        gbc.gridy = baris + 1;
        gbc.insets = new Insets(0, 0, 6, 0);
        form.add(spnKapasitas, gbc);
        baris += 2;

        // Checkbox sopir (diberi margin aman agar kotak centang tidak terpotong)
        chkSopir.setMargin(new Insets(2, 4, 2, 4));
        gbc.gridy = baris;
        gbc.insets = new Insets(4, 2, 8, 0);
        form.add(chkSopir, gbc);
        baris++;

        // Hint
        JLabel lblHint = new JLabel(
                "<html><small style='color:#8B8FA8;'>Tarif diisi angka tanpa titik, contoh: 350000.<br>"
                + "Kode kendaraan dikunci saat mengubah data.</small></html>");
        gbc.gridy = baris;
        gbc.insets = new Insets(2, 0, 12, 0);
        form.add(lblHint, gbc);
        baris++;

        // Tombol Tambah Kendaraan langsung di bawah pengisian form
        btnTambah.setPreferredSize(new Dimension(0, 40));
        gbc.gridy = baris;
        gbc.insets = new Insets(4, 0, 4, 0);
        form.add(btnTambah, gbc);

        return form;
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
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Search bar
        JPanel panelCari = new JPanel(new BorderLayout(8, 0));
        panelCari.setOpaque(false);

        JLabel lblCari = new JLabel("🔍");
        lblCari.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

        txtCari.setPreferredSize(new Dimension(0, 36));
        // placeholder teks manual via label
        panelCari.add(lblCari, BorderLayout.WEST);
        panelCari.add(txtCari, BorderLayout.CENTER);

        JLabel lblCariHint = AppTheme.makeFormLabel("Cari kode, merk, plat, atau jenis...");
        panelCari.add(lblCariHint, BorderLayout.EAST);

        // Tabel
        AppTheme.styleTable(tabel);
        tabel.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] lebarKolom = {85, 75, 160, 120, 130, 95, 320};
        for (int i = 0; i < lebarKolom.length && i < tabel.getColumnCount(); i++) {
            tabel.getColumnModel().getColumn(i).setPreferredWidth(lebarKolom[i]);
        }
        UiHelper.pasangTooltipTabel(tabel);

        // Status renderer untuk kolom "Status"
        tabel.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

        JScrollPane scroll = AppTheme.makeScrollPane(tabel);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        panel.add(panelCari, BorderLayout.NORTH);
        panel.add(scroll,    BorderLayout.CENTER);
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
        panel.add(btnUbah);
        return panel;
    }

    // ---------------------------------------------------------------
    // AKSI
    // ---------------------------------------------------------------
    private void pasangAksi() {
        btnTambah.addActionListener(e -> simpanKendaraanBaru());
        btnUbah.addActionListener(e -> ubahKendaraan());
        btnHapus.addActionListener(e -> hapusKendaraan());
        btnBersih.addActionListener(e -> bersihkanForm());
        btnRefresh.addActionListener(e -> muatData());
        cmbJenis.addActionListener(e -> sesuaikanFormJenis());

        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            if (tabel.getSelectedRow() < 0) {
                kodeTerpilih = null;
                txtKode.setEditable(true);
                perbaruiTombol();
            } else {
                tampilkanDataTerpilih();
            }
        });

        txtCari.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { terapkanFilter(); }
            @Override public void removeUpdate(DocumentEvent e)  { terapkanFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { terapkanFilter(); }
        });
    }

    private void sesuaikanFormJenis() {
        String jenis = (String) cmbJenis.getSelectedItem();
        boolean mobil = "Mobil".equals(jenis);

        chkSopir.setEnabled(mobil);
        if (!mobil) chkSopir.setSelected(false);

        if ("Motor".equals(jenis)) {
            lblKapasitas.setText("KAPASITAS MESIN (CC)");
            spnKapasitas.setEnabled(true);
            if (((Number) spnKapasitas.getValue()).intValue() > 2000) spnKapasitas.setValue(150);
        } else if ("Bus".equals(jenis)) {
            lblKapasitas.setText("JUMLAH KURSI");
            spnKapasitas.setEnabled(true);
            if (((Number) spnKapasitas.getValue()).intValue() > 100) spnKapasitas.setValue(40);
        } else {
            lblKapasitas.setText("KAPASITAS (KHUSUS MOTOR/BUS)");
            spnKapasitas.setEnabled(false);
        }
    }

    private void tampilkanDataTerpilih() {
        int barisView = tabel.getSelectedRow();
        if (barisView < 0) return;
        int barisModel = tabel.convertRowIndexToModel(barisView);
        Kendaraan k = modelTabel.getKendaraan(barisModel);
        if (k == null) return;

        kodeTerpilih   = k.getKodeKendaraan();
        tersediaTerpilih = k.isTersedia();

        txtKode.setText(k.getKodeKendaraan());
        txtKode.setEditable(false);
        cmbJenis.setSelectedItem(KendaraanTableModel.namaJenis(k));
        txtMerk.setText(k.getMerk());
        txtPlat.setText(k.getPlatNomor());
        txtTarif.setText(String.format("%.0f", k.getTarifHarian()));

        if (k instanceof Mobil) {
            chkSopir.setSelected(((Mobil) k).isDenganSopir());
            spnKapasitas.setValue(1000);
        } else if (k instanceof Motor) {
            spnKapasitas.setValue(((Motor) k).getKapasitasCC());
        } else if (k instanceof Bus) {
            spnKapasitas.setValue(((Bus) k).getJumlahKursi());
        }
        perbaruiTombol();
    }

    private void simpanKendaraanBaru() {
        try {
            controller.tambahKendaraan(
                    (String) cmbJenis.getSelectedItem(),
                    txtKode.getText(), txtMerk.getText(), txtPlat.getText(),
                    ambilTarif(), true, chkSopir.isSelected(),
                    ambilKapasitas(), ambilKapasitas());
            UiHelper.tampilkanInfo(this, "Data kendaraan berhasil disimpan.");
            muatData();
        } catch (InputTidakValidException ex) {
            UiHelper.tampilkanError(this, ex.getMessage());
        } catch (SQLException ex) {
            UiHelper.tampilkanError(this, "Gagal menyimpan data kendaraan:\n" + ex.getMessage());
        }
    }

    private void ubahKendaraan() {
        if (kodeTerpilih == null) {
            UiHelper.tampilkanError(this, "Pilih data kendaraan pada tabel terlebih dahulu.");
            return;
        }
        try {
            controller.ubahKendaraan(
                    kodeTerpilih, (String) cmbJenis.getSelectedItem(),
                    txtKode.getText(), txtMerk.getText(), txtPlat.getText(),
                    ambilTarif(), tersediaTerpilih, chkSopir.isSelected(),
                    ambilKapasitas(), ambilKapasitas());
            UiHelper.tampilkanInfo(this, "Data kendaraan berhasil diperbarui.");
            muatData();
        } catch (InputTidakValidException ex) {
            UiHelper.tampilkanError(this, ex.getMessage());
        } catch (SQLException ex) {
            UiHelper.tampilkanError(this, "Gagal memperbarui data kendaraan:\n" + ex.getMessage());
        }
    }

    private void hapusKendaraan() {
        if (kodeTerpilih == null) {
            UiHelper.tampilkanError(this, "Pilih data kendaraan pada tabel terlebih dahulu.");
            return;
        }
        int pilihan = JOptionPane.showConfirmDialog(this,
                "Hapus kendaraan " + kodeTerpilih + " - " + txtMerk.getText() + "?\n"
                + "Data yang sudah dihapus tidak dapat dikembalikan.",
                "Konfirmasi Hapus Kendaraan",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (pilihan != JOptionPane.YES_OPTION) return;
        try {
            controller.hapusKendaraan(kodeTerpilih);
            UiHelper.tampilkanInfo(this, "Data kendaraan berhasil dihapus.");
            muatData();
        } catch (InputTidakValidException ex) {
            UiHelper.tampilkanError(this, ex.getMessage());
        } catch (SQLException ex) {
            UiHelper.tampilkanError(this, "Gagal menghapus data kendaraan:\n" + ex.getMessage());
        }
    }

    private void bersihkanForm() {
        kodeTerpilih     = null;
        tersediaTerpilih = true;

        txtKode.setText(""); txtKode.setEditable(true);
        cmbJenis.setSelectedIndex(0);
        txtMerk.setText(""); txtPlat.setText(""); txtTarif.setText("");
        spnKapasitas.setValue(1000);
        chkSopir.setSelected(false);

        tabel.clearSelection();
        sesuaikanFormJenis();
        perbaruiTombol();
        txtKode.requestFocusInWindow();
        if (scrollForm != null) {
            javax.swing.SwingUtilities.invokeLater(() ->
                    scrollForm.getViewport().setViewPosition(new java.awt.Point(0, 0)));
        }
    }

    private void perbaruiTombol() {
        boolean adaData = kodeTerpilih != null;
        btnUbah.setEnabled(adaData);
        btnHapus.setEnabled(adaData);
        btnTambah.setEnabled(!adaData);
    }

    public void muatData() {
        try {
            semuaKendaraan = controller.ambilSemuaKendaraan();
            terapkanFilter();
            bersihkanForm();
            if (scrollForm != null) {
                javax.swing.SwingUtilities.invokeLater(() ->
                        scrollForm.getViewport().setViewPosition(new java.awt.Point(0, 0)));
            }
        } catch (SQLException ex) {
            UiHelper.tampilkanError(this,
                    "Gagal memuat data kendaraan dari database:\n" + ex.getMessage());
        }
    }

    private void terapkanFilter() {
        String kw = txtCari.getText().trim().toLowerCase();
        List<Kendaraan> hasil = new ArrayList<>();
        for (Kendaraan k : semuaKendaraan) {
            if (kw.isEmpty()
                    || k.getKodeKendaraan().toLowerCase().contains(kw)
                    || k.getMerk().toLowerCase().contains(kw)
                    || k.getPlatNomor().toLowerCase().contains(kw)
                    || k.getJenis().toLowerCase().contains(kw)) {
                hasil.add(k);
            }
        }
        modelTabel.setData(hasil);
        tabel.clearSelection();
        kodeTerpilih = null;
        txtKode.setEditable(true);
        perbaruiTombol();
        lblStatus.setText(hasil.size() + " dari " + semuaKendaraan.size() + " kendaraan");
    }

    private double ambilTarif() {
        String teks = txtTarif.getText().trim().replace(".", "").replace(",", ".");
        if (teks.isEmpty()) throw new InputTidakValidException("Tarif Harian", "Tarif harian wajib diisi.");
        try {
            return Double.parseDouble(teks);
        } catch (NumberFormatException ex) {
            throw new InputTidakValidException("Tarif Harian", "Tarif harian harus berupa angka, contoh: 350000");
        }
    }

    private int ambilKapasitas() {
        return ((Number) spnKapasitas.getValue()).intValue();
    }

    private static void styleCheckBox(JCheckBox cb) {
        cb.setFont(AppTheme.FONT_BODY);
        cb.setForeground(AppTheme.TEXT_DARK);
        cb.setOpaque(false);
        cb.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }

    // ---------------------------------------------------------------
    // STATUS CELL RENDERER
    // ---------------------------------------------------------------
    private static class StatusCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public java.awt.Component getTableCellRendererComponent(
                JTable table, Object value, boolean selected, boolean focus, int row, int col) {
            String text = value == null ? "" : value.toString();
            boolean tersedia = "Tersedia".equals(text);

            AppTheme.StatusBadge badge = new AppTheme.StatusBadge(
                    text, tersedia ? AppTheme.SAGE_GREEN : AppTheme.CORAL);
            badge.setBackground(selected ? AppTheme.ROW_SELECTED :
                    (row % 2 == 0 ? AppTheme.CARD_BG : AppTheme.ROW_ALT));
            badge.setOpaque(true);

            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
            wrapper.setBackground(selected ? AppTheme.ROW_SELECTED :
                    (row % 2 == 0 ? AppTheme.CARD_BG : AppTheme.ROW_ALT));
            wrapper.add(badge);
            return wrapper;
        }
    }
}
