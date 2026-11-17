package com.rentalkendaraan.model;

/**
 * Ringkasan angka untuk halaman dashboard.
 * Objek ini hanya membawa data dari controller ke view (read-only).
 */
public class StatistikDashboard {

    private final int totalKendaraan;
    private final int jumlahTersedia;
    private final int jumlahDisewa;
    private final int transaksiAktif;
    private final int transaksiSelesai;
    private final double totalPendapatan;

    public StatistikDashboard(int totalKendaraan, int jumlahTersedia, int jumlahDisewa,
                              int transaksiAktif, int transaksiSelesai,
                              double totalPendapatan) {
        this.totalKendaraan = totalKendaraan;
        this.jumlahTersedia = jumlahTersedia;
        this.jumlahDisewa = jumlahDisewa;
        this.transaksiAktif = transaksiAktif;
        this.transaksiSelesai = transaksiSelesai;
        this.totalPendapatan = totalPendapatan;
    }

    public int getTotalKendaraan() {
        return totalKendaraan;
    }

    public int getJumlahTersedia() {
        return jumlahTersedia;
    }

    public int getJumlahDisewa() {
        return jumlahDisewa;
    }

    public int getTransaksiAktif() {
        return transaksiAktif;
    }

    public int getTransaksiSelesai() {
        return transaksiSelesai;
    }

    public double getTotalPendapatan() {
        return totalPendapatan;
    }
}
