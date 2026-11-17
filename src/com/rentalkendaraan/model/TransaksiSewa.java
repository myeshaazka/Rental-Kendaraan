package com.rentalkendaraan.model;

import java.time.LocalDate;

import com.rentalkendaraan.exception.InputTidakValidException;

/**
 * Model transaksi penyewaan dan pengembalian kendaraan.
 *
 * <p>Mengimplementasikan {@link DapatDisewa} sehingga proses sewa/kembali
 * selalu sinkron dengan status kendaraan terkait.</p>
 */
public class TransaksiSewa implements DapatDisewa {

    public static final String STATUS_AKTIF = "AKTIF";
    public static final String STATUS_SELESAI = "SELESAI";

    /** Biaya asuransi tambahan per hari (opsional, dicentang pengguna). */
    public static final double BIAYA_ASURANSI_PER_HARI = 25_000.0;

    private int idTransaksi;
    private Kendaraan kendaraan;
    private Penyewa penyewa;
    private int lamaSewa;
    private LocalDate tanggalSewa;
    private LocalDate tanggalKembali;
    private double totalBiaya;
    private String status;
    private String metodePembayaran;
    private boolean asuransiTambahan;

    /**
     * Constructor untuk transaksi sewa baru. Total biaya langsung dihitung
     * menggunakan referensi {@link Kendaraan} (polimorfisme).
     */
    public TransaksiSewa(Kendaraan kendaraan, Penyewa penyewa, int lamaSewa,
                         LocalDate tanggalSewa, String metodePembayaran,
                         boolean asuransiTambahan) {
        setKendaraan(kendaraan);
        setPenyewa(penyewa);
        setLamaSewa(lamaSewa);
        setTanggalSewa(tanggalSewa);
        setMetodePembayaran(metodePembayaran);
        setAsuransiTambahan(asuransiTambahan);
        this.status = STATUS_AKTIF;
        this.tanggalKembali = null;
        this.totalBiaya = hitungTotalBiaya(lamaSewa);
    }

    /**
     * Constructor untuk memetakan data yang sudah tersimpan di database
     * (total biaya dan status diambil apa adanya dari tabel).
     */
    public TransaksiSewa(int idTransaksi, Kendaraan kendaraan, Penyewa penyewa,
                         int lamaSewa, LocalDate tanggalSewa, LocalDate tanggalKembali,
                         double totalBiaya, String status, String metodePembayaran,
                         boolean asuransiTambahan) {
        setIdTransaksi(idTransaksi);
        setKendaraan(kendaraan);
        setPenyewa(penyewa);
        setLamaSewa(lamaSewa);
        setTanggalSewa(tanggalSewa);
        setTanggalKembali(tanggalKembali);
        setTotalBiaya(totalBiaya);
        setStatus(status);
        setMetodePembayaran(metodePembayaran);
        setAsuransiTambahan(asuransiTambahan);
    }

    /* ==================== PERHITUNGAN BIAYA ==================== */

    /**
     * Menghitung total biaya transaksi berdasarkan aturan kendaraan
     * (overriding {@code hitungSewa(int)}) ditambah asuransi opsional.
     */
    public double hitungTotalBiaya(int hari) {
        validasiHari(hari);
        if (kendaraan == null) {
            throw new InputTidakValidException("Kendaraan",
                    "Kendaraan belum dipilih untuk transaksi ini.");
        }
        double total = kendaraan.hitungSewa(hari);
        if (asuransiTambahan) {
            total += BIAYA_ASURANSI_PER_HARI * hari;
        }
        return total;
    }

    /**
     * Overloading: menghitung total biaya dengan diskon persentase tambahan.
     */
    public double hitungTotalBiaya(int hari, double diskonPersen) {
        validasiHari(hari);
        if (kendaraan == null) {
            throw new InputTidakValidException("Kendaraan",
                    "Kendaraan belum dipilih untuk transaksi ini.");
        }
        double total = kendaraan.hitungSewa(hari, diskonPersen);
        if (asuransiTambahan) {
            total += BIAYA_ASURANSI_PER_HARI * hari;
        }
        return total;
    }

    private void validasiHari(int hari) {
        if (hari <= 0) {
            throw new InputTidakValidException("Lama Sewa",
                    "Lama sewa harus lebih besar dari 0 hari.");
        }
    }

    /* ==================== IMPLEMENTASI DapatDisewa ==================== */

    @Override
    public void prosesSewa() {
        if (kendaraan == null) {
            throw new InputTidakValidException("Kendaraan",
                    "Kendaraan belum dipilih untuk transaksi ini.");
        }
        kendaraan.prosesSewa();
        this.status = STATUS_AKTIF;
        this.tanggalKembali = null;
    }

    @Override
    public void prosesKembali() {
        if (STATUS_SELESAI.equals(status)) {
            throw new InputTidakValidException("Status Transaksi",
                    "Transaksi ini sudah selesai dan kendaraannya sudah dikembalikan.");
        }
        if (kendaraan == null) {
            throw new InputTidakValidException("Kendaraan",
                    "Kendaraan belum dipilih untuk transaksi ini.");
        }
        kendaraan.prosesKembali();
        this.status = STATUS_SELESAI;
        this.tanggalKembali = LocalDate.now();
    }

    /**
     * Untuk transaksi, {@code isTersedia()} berarti transaksi masih berjalan
     * (kendaraan belum dikembalikan).
     */
    @Override
    public boolean isTersedia() {
        return STATUS_AKTIF.equals(status);
    }

    /* ==================== GETTER & SETTER ==================== */

    public int getIdTransaksi() {
        return idTransaksi;
    }

    public void setIdTransaksi(int idTransaksi) {
        if (idTransaksi < 0) {
            throw new InputTidakValidException("ID Transaksi",
                    "ID transaksi tidak boleh negatif.");
        }
        this.idTransaksi = idTransaksi;
    }

    public Kendaraan getKendaraan() {
        return kendaraan;
    }

    public void setKendaraan(Kendaraan kendaraan) {
        if (kendaraan == null) {
            throw new InputTidakValidException("Kendaraan",
                    "Kendaraan wajib dipilih untuk transaksi sewa.");
        }
        this.kendaraan = kendaraan;
    }

    public Penyewa getPenyewa() {
        return penyewa;
    }

    public void setPenyewa(Penyewa penyewa) {
        if (penyewa == null) {
            throw new InputTidakValidException("Penyewa",
                    "Data penyewa wajib diisi untuk transaksi sewa.");
        }
        this.penyewa = penyewa;
    }

    public int getLamaSewa() {
        return lamaSewa;
    }

    public void setLamaSewa(int lamaSewa) {
        validasiHari(lamaSewa);
        if (lamaSewa > 365) {
            throw new InputTidakValidException("Lama Sewa",
                    "Lama sewa maksimal 365 hari.");
        }
        this.lamaSewa = lamaSewa;
    }

    public LocalDate getTanggalSewa() {
        return tanggalSewa;
    }

    public void setTanggalSewa(LocalDate tanggalSewa) {
        if (tanggalSewa == null) {
            throw new InputTidakValidException("Tanggal Sewa",
                    "Tanggal sewa wajib diisi.");
        }
        this.tanggalSewa = tanggalSewa;
    }

    public LocalDate getTanggalKembali() {
        return tanggalKembali;
    }

    public void setTanggalKembali(LocalDate tanggalKembali) {
        if (tanggalKembali != null && tanggalSewa != null
                && tanggalKembali.isBefore(tanggalSewa)) {
            throw new InputTidakValidException("Tanggal Kembali",
                    "Tanggal kembali tidak boleh lebih awal dari tanggal sewa.");
        }
        this.tanggalKembali = tanggalKembali;
    }

    public double getTotalBiaya() {
        return totalBiaya;
    }

    public void setTotalBiaya(double totalBiaya) {
        if (Double.isNaN(totalBiaya) || Double.isInfinite(totalBiaya) || totalBiaya < 0) {
            throw new InputTidakValidException("Total Biaya",
                    "Total biaya tidak boleh negatif atau bernilai tidak valid.");
        }
        this.totalBiaya = totalBiaya;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new InputTidakValidException("Status",
                    "Status transaksi wajib diisi.");
        }
        String nilai = status.trim().toUpperCase();
        if (!STATUS_AKTIF.equals(nilai) && !STATUS_SELESAI.equals(nilai)) {
            throw new InputTidakValidException("Status",
                    "Status transaksi hanya boleh AKTIF atau SELESAI.");
        }
        this.status = nilai;
    }

    public String getMetodePembayaran() {
        return metodePembayaran;
    }

    public void setMetodePembayaran(String metodePembayaran) {
        if (metodePembayaran == null || metodePembayaran.trim().isEmpty()) {
            throw new InputTidakValidException("Metode Pembayaran",
                    "Metode pembayaran wajib dipilih.");
        }
        String nilai = metodePembayaran.trim();
        if (nilai.length() > 20) {
            throw new InputTidakValidException("Metode Pembayaran",
                    "Metode pembayaran maksimal 20 karakter.");
        }
        this.metodePembayaran = nilai;
    }

    public boolean isAsuransiTambahan() {
        return asuransiTambahan;
    }

    public void setAsuransiTambahan(boolean asuransiTambahan) {
        this.asuransiTambahan = asuransiTambahan;
    }

    @Override
    public String toString() {
        return "TRX-" + idTransaksi + " | " + kendaraan.getKodeKendaraan()
                + " | " + penyewa.getNama() + " | " + status;
    }
}
