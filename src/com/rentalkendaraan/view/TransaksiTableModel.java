package com.rentalkendaraan.view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.rentalkendaraan.model.TransaksiSewa;

/**
 * Table model untuk daftar transaksi sewa (dipakai halaman Transaksi
 * dan tabel "Kendaraan Sedang Disewa" pada dashboard).
 */
public class TransaksiTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private static final String[] KOLOM = {
            "ID", "Kendaraan", "Penyewa", "Hari", "Periode Sewa", "Total Biaya",
            "Status", "Metode Bayar", "Asuransi"
    };

    private static final DateTimeFormatter FORMAT_TANGGAL =
            DateTimeFormatter.ofPattern("dd-MM-yy");

    private List<TransaksiSewa> data = new ArrayList<>();

    public void setData(List<TransaksiSewa> data) {
        this.data = (data == null) ? new ArrayList<>() : new ArrayList<>(data);
        fireTableDataChanged();
    }

    public TransaksiSewa getTransaksi(int baris) {
        if (baris < 0 || baris >= data.size()) {
            return null;
        }
        return data.get(baris);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return KOLOM.length;
    }

    @Override
    public String getColumnName(int column) {
        return KOLOM[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        TransaksiSewa transaksi = data.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return transaksi.getIdTransaksi();
            case 1:
                return transaksi.getKendaraan().getKodeKendaraan() + " - "
                        + transaksi.getKendaraan().getMerk()
                        + " (" + transaksi.getKendaraan().getPlatNomor() + ")";
            case 2:
                return transaksi.getPenyewa().getNama();
            case 3:
                return transaksi.getLamaSewa();
            case 4:
                if (transaksi.getTanggalKembali() == null) {
                    return formatTanggal(transaksi.getTanggalSewa()) + " - (aktif)";
                }
                return formatTanggal(transaksi.getTanggalSewa()) + " - "
                        + formatTanggal(transaksi.getTanggalKembali());
            case 5:
                return UiHelper.rupiah(transaksi.getTotalBiaya());
            case 6:
                return transaksi.getStatus();
            case 7:
                return transaksi.getMetodePembayaran();
            case 8:
                return transaksi.isAsuransiTambahan() ? "Ya" : "Tidak";
            default:
                return "";
        }
    }

    private String formatTanggal(LocalDate tanggal) {
        return (tanggal == null) ? "-" : tanggal.format(FORMAT_TANGGAL);
    }
}
