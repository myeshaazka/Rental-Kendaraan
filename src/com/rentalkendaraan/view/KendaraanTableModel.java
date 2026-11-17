package com.rentalkendaraan.view;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.rentalkendaraan.model.Bus;
import com.rentalkendaraan.model.Kendaraan;
import com.rentalkendaraan.model.Mobil;
import com.rentalkendaraan.model.Motor;

/**
 * Table model untuk daftar kendaraan (dipakai halaman Manajemen Kendaraan).
 */
public class KendaraanTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private static final String[] KOLOM = {
            "Kode", "Jenis", "Merk", "Plat Nomor", "Tarif/Hari", "Status", "Spesifikasi"
    };

    private List<Kendaraan> data = new ArrayList<>();

    public void setData(List<Kendaraan> data) {
        this.data = (data == null) ? new ArrayList<>() : new ArrayList<>(data);
        fireTableDataChanged();
    }

    public Kendaraan getKendaraan(int baris) {
        if (baris < 0 || baris >= data.size()) {
            return null;
        }
        return data.get(baris);
    }

    /** Nama jenis yang ramah untuk ditampilkan di UI. */
    public static String namaJenis(Kendaraan kendaraan) {
        if (kendaraan instanceof Mobil) {
            return "Mobil";
        }
        if (kendaraan instanceof Motor) {
            return "Motor";
        }
        if (kendaraan instanceof Bus) {
            return "Bus";
        }
        return "-";
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
        Kendaraan kendaraan = data.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return kendaraan.getKodeKendaraan();
            case 1:
                return namaJenis(kendaraan);
            case 2:
                return kendaraan.getMerk();
            case 3:
                return kendaraan.getPlatNomor();
            case 4:
                return UiHelper.rupiah(kendaraan.getTarifHarian());
            case 5:
                return kendaraan.isTersedia() ? "Tersedia" : "Disewa";
            case 6:
                return kendaraan.getDetailSpesifik();
            default:
                return "";
        }
    }
}
