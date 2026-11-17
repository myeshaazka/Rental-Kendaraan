package com.rentalkendaraan.view;

import java.awt.Component;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JOptionPane;

/**
 * Kumpulan helper tampilan (format rupiah, dialog error/informasi/konfirmasi)
 * agar kode View tidak berulang.
 */
final class UiHelper {

    private static final NumberFormat FORMAT_RUPIAH =
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));

    private UiHelper() {
    }

    static String rupiah(double nilai) {
        return FORMAT_RUPIAH.format(nilai);
    }

    static void tampilkanError(Component parent, String pesan) {
        JOptionPane.showMessageDialog(parent, pesan,
                "Terjadi Kesalahan", JOptionPane.ERROR_MESSAGE);
    }

    static void tampilkanInfo(Component parent, String pesan) {
        JOptionPane.showMessageDialog(parent, pesan,
                "Informasi", JOptionPane.INFORMATION_MESSAGE);
    }

    static boolean konfirmasi(Component parent, String judul, String pesan) {
        int pilihan = JOptionPane.showConfirmDialog(parent, pesan, judul,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return pilihan == JOptionPane.YES_OPTION;
    }

    /**
     * Menampilkan tooltip berisi isi sel penuh, berguna ketika teks terpotong
     * karena lebar kolom terbatas.
     */
    static void pasangTooltipTabel(javax.swing.JTable tabel) {
        tabel.setToolTipText("");
        tabel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int baris = tabel.rowAtPoint(e.getPoint());
                int kolom = tabel.columnAtPoint(e.getPoint());
                if (baris < 0 || kolom < 0) {
                    tabel.setToolTipText(null);
                    return;
                }
                Object nilai = tabel.getValueAt(baris, kolom);
                String teks = (nilai == null) ? "" : nilai.toString();
                tabel.setToolTipText(teks.isEmpty() ? null : teks);
            }
        });
    }
}
