package com.rentalkendaraan.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.rentalkendaraan.config.DatabaseConnection;
import com.rentalkendaraan.dao.KendaraanDAO;
import com.rentalkendaraan.dao.TransaksiDAO;
import com.rentalkendaraan.exception.DataTidakDitemukanException;
import com.rentalkendaraan.exception.InputTidakValidException;
import com.rentalkendaraan.exception.KendaraanTidakTersediaException;
import com.rentalkendaraan.model.Bus;
import com.rentalkendaraan.model.Kendaraan;
import com.rentalkendaraan.model.Mobil;
import com.rentalkendaraan.model.Motor;
import com.rentalkendaraan.model.Penyewa;
import com.rentalkendaraan.model.StatistikDashboard;
import com.rentalkendaraan.model.TransaksiSewa;

/**
 * Controller utama yang menghubungkan View (Swing) dengan Model dan DAO.
 *
 * <p>Seluruh aturan bisnis (validasi, perhitungan, orkestrasi penyimpanan)
 * berada di kelas ini sehingga event listener pada View tidak berisi query
 * SQL maupun perhitungan bisnis.</p>
 */
public class RentalController {

    /** Akun login default aplikasi (studi kasus, bukan autentikasi produksi). */
    private static final String USERNAME_DEFAULT = "admin";
    private static final String PASSWORD_DEFAULT = "admin123";

    private final KendaraanDAO kendaraanDAO;
    private final TransaksiDAO transaksiDAO;

    public RentalController() {
        this(new KendaraanDAO(), new TransaksiDAO());
    }

    /** Constructor untuk dependency injection saat pengujian. */
    public RentalController(KendaraanDAO kendaraanDAO, TransaksiDAO transaksiDAO) {
        this.kendaraanDAO = kendaraanDAO;
        this.transaksiDAO = transaksiDAO;
    }

    /* ==================== AUTENTIKASI & KONEKSI ==================== */

    /**
     * Memvalidasi kredensial login.
     *
     * @throws InputTidakValidException bila username/password kosong
     */
    public boolean login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new InputTidakValidException("Username",
                    "Username wajib diisi dan tidak boleh kosong.");
        }
        if (password == null || password.isEmpty()) {
            throw new InputTidakValidException("Password",
                    "Password wajib diisi dan tidak boleh kosong.");
        }
        return USERNAME_DEFAULT.equalsIgnoreCase(username.trim())
                && PASSWORD_DEFAULT.equals(password);
    }

    /** Menguji koneksi ke Supabase; melempar SQLException bila gagal. */
    public void tesKoneksi() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (!conn.isValid(10)) {
                throw new SQLException("Koneksi terbentuk tetapi tidak valid "
                        + "(timeout saat memeriksa koneksi).");
            }
        }
    }

    public String infoKoneksi() {
        return DatabaseConnection.getInfoKoneksi();
    }

    /* ==================== DATA KENDARAAN ==================== */

    public List<Kendaraan> ambilSemuaKendaraan() throws SQLException {
        return kendaraanDAO.findAll();
    }

    public List<Kendaraan> ambilKendaraanTersedia() throws SQLException {
        return kendaraanDAO.findTersedia();
    }

    public Map<String, Kendaraan> ambilPetaKendaraan() throws SQLException {
        return kendaraanDAO.findAllAsMap();
    }

    /**
     * Menambahkan kendaraan baru (Mobil/Motor/Bus) ke database.
     *
     * @param jenis "Mobil", "Motor", atau "Bus" (case-insensitive)
     */
    public void tambahKendaraan(String jenis, String kodeKendaraan, String merk,
                                String platNomor, double tarifHarian, boolean tersedia,
                                boolean denganSopir, int kapasitasCC, int jumlahKursi)
            throws SQLException {
        Kendaraan kendaraan = buatKendaraan(jenis, kodeKendaraan, merk, platNomor,
                tarifHarian, tersedia, denganSopir, kapasitasCC, jumlahKursi);
        try {
            kendaraanDAO.insert(kendaraan);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new InputTidakValidException("Kendaraan",
                        "Kode kendaraan atau plat nomor sudah terdaftar. "
                                + "Gunakan kode/plat lain.");
            }
            throw e;
        }
    }

    /**
     * Memperbarui data kendaraan. Kode kendaraan (primary key) tidak boleh
     * diubah agar relasi transaksi tetap konsisten.
     */
    public void ubahKendaraan(String kodeLama, String jenis, String kodeKendaraan,
                              String merk, String platNomor, double tarifHarian,
                              boolean tersedia, boolean denganSopir,
                              int kapasitasCC, int jumlahKursi) throws SQLException {
        if (kodeLama == null || kodeLama.trim().isEmpty()) {
            throw new InputTidakValidException("Kode Kendaraan",
                    "Pilih kendaraan yang akan diubah terlebih dahulu.");
        }
        Kendaraan kendaraan = buatKendaraan(jenis, kodeKendaraan, merk, platNomor,
                tarifHarian, tersedia, denganSopir, kapasitasCC, jumlahKursi);
        if (!kodeLama.equalsIgnoreCase(kendaraan.getKodeKendaraan())) {
            throw new InputTidakValidException("Kode Kendaraan",
                    "Kode kendaraan tidak boleh diubah saat mengubah data.");
        }
        try {
            kendaraanDAO.update(kendaraan);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new InputTidakValidException("Kendaraan",
                        "Plat nomor sudah dipakai kendaraan lain. Gunakan plat lain.");
            }
            throw e;
        }
    }

    /**
     * Menghapus kendaraan. Bila kendaraan masih direferensikan transaksi,
     * database akan menolak (foreign key) dan pesan yang ramah ditampilkan.
     */
    public void hapusKendaraan(String kodeKendaraan) throws SQLException {
        if (kodeKendaraan == null || kodeKendaraan.trim().isEmpty()) {
            throw new InputTidakValidException("Kode Kendaraan",
                    "Pilih kendaraan yang akan dihapus terlebih dahulu.");
        }
        try {
            kendaraanDAO.delete(kodeKendaraan);
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new InputTidakValidException("Kendaraan",
                        "Kendaraan tidak dapat dihapus karena masih memiliki riwayat "
                                + "transaksi. Hapus riwayat transaksinya terlebih dahulu.");
            }
            throw e;
        }
    }

    /** Factory Method: membentuk objek subclass yang tepat dari input form. */
    private Kendaraan buatKendaraan(String jenis, String kodeKendaraan, String merk,
                                    String platNomor, double tarifHarian, boolean tersedia,
                                    boolean denganSopir, int kapasitasCC, int jumlahKursi) {
        String jenisBersih = (jenis == null) ? "" : jenis.trim().toUpperCase();
        switch (jenisBersih) {
            case "MOBIL":
                return new Mobil(kodeKendaraan, merk, platNomor, tarifHarian,
                        tersedia, denganSopir);
            case "MOTOR":
                return new Motor(kodeKendaraan, merk, platNomor, tarifHarian,
                        tersedia, kapasitasCC);
            case "BUS":
                return new Bus(kodeKendaraan, merk, platNomor, tarifHarian,
                        tersedia, jumlahKursi);
            default:
                throw new InputTidakValidException("Jenis Kendaraan",
                        "Jenis kendaraan harus Mobil, Motor, atau Bus.");
        }
    }

    /* ==================== TRANSAKSI SEWA ==================== */

    public List<TransaksiSewa> ambilSemuaTransaksi() throws SQLException {
        return transaksiDAO.findAll();
    }

    /**
     * Membuat transaksi sewa baru.
     *
     * <p>Alur: validasi kendaraan -> bentuk objek Penyewa & TransaksiSewa
     * (total biaya dihitung secara polimorfik) -> tandai kendaraan disewa
     * -> simpan ke database secara atomik.</p>
     */
    public void buatTransaksi(String kodeKendaraan, String namaPenyewa,
                              String nomorKtp, String nomorTelepon, String alamat,
                              int lamaSewa, String metodePembayaran,
                              boolean asuransiTambahan) throws SQLException {
        if (kodeKendaraan == null || kodeKendaraan.trim().isEmpty()) {
            throw new InputTidakValidException("Kendaraan",
                    "Pilih kendaraan yang akan disewa terlebih dahulu.");
        }

        Kendaraan kendaraan = kendaraanDAO.findByKode(kodeKendaraan);
        if (kendaraan == null) {
            throw new DataTidakDitemukanException("Kendaraan dengan kode "
                    + kodeKendaraan + " tidak ditemukan di database.");
        }
        if (!kendaraan.isTersedia()) {
            throw new KendaraanTidakTersediaException(kendaraan.getKodeKendaraan(),
                    "Kendaraan " + kendaraan.getKodeKendaraan() + " (" + kendaraan.getMerk()
                            + ") sedang disewa. Silakan pilih kendaraan lain.");
        }

        Penyewa penyewa = new Penyewa(namaPenyewa, nomorKtp, nomorTelepon, alamat);
        TransaksiSewa transaksi = new TransaksiSewa(kendaraan, penyewa, lamaSewa,
                LocalDate.now(), metodePembayaran, asuransiTambahan);

        // Menandai kendaraan tidak tersedia melalui kontrak interface.
        transaksi.prosesSewa();

        transaksiDAO.simpanSewaBaru(transaksi);
    }

    /** Memproses pengembalian kendaraan dan mengubah statusnya menjadi tersedia. */
    public void kembalikanKendaraan(int idTransaksi) throws SQLException {
        TransaksiSewa transaksi = transaksiDAO.findById(idTransaksi);
        if (transaksi == null) {
            throw new DataTidakDitemukanException("Transaksi dengan ID " + idTransaksi
                    + " tidak ditemukan di database.");
        }
        if (!transaksi.isTersedia()) {
            throw new InputTidakValidException("Status Transaksi",
                    "Transaksi ini sudah selesai; kendaraan sudah dikembalikan.");
        }

        transaksi.prosesKembali();
        transaksiDAO.prosesPengembalian(transaksi);
    }

    /** Menghapus riwayat transaksi (kendaraan otomatis dilepas bila masih aktif). */
    public void hapusTransaksi(int idTransaksi) throws SQLException {
        if (idTransaksi <= 0) {
            throw new InputTidakValidException("ID Transaksi",
                    "Pilih transaksi yang akan dihapus terlebih dahulu.");
        }
        transaksiDAO.hapus(idTransaksi);
    }

    /**
     * Estimasi biaya untuk form transaksi. Perhitungan tetap berada di
     * controller/model, bukan di event listener View.
     */
    public double hitungEstimasi(Kendaraan kendaraan, int lamaSewa,
                                 boolean asuransiTambahan) {
        if (kendaraan == null) {
            throw new InputTidakValidException("Kendaraan",
                    "Pilih kendaraan terlebih dahulu.");
        }
        if (lamaSewa <= 0) {
            throw new InputTidakValidException("Lama Sewa",
                    "Lama sewa minimal 1 hari.");
        }
        double total = kendaraan.hitungSewa(lamaSewa);
        if (asuransiTambahan) {
            total += TransaksiSewa.BIAYA_ASURANSI_PER_HARI * lamaSewa;
        }
        return total;
    }

    /** Overloading estimasi biaya dengan diskon persentase. */
    public double hitungEstimasi(Kendaraan kendaraan, int lamaSewa,
                                 boolean asuransiTambahan, double diskonPersen) {
        if (kendaraan == null) {
            throw new InputTidakValidException("Kendaraan",
                    "Pilih kendaraan terlebih dahulu.");
        }
        if (lamaSewa <= 0) {
            throw new InputTidakValidException("Lama Sewa",
                    "Lama sewa minimal 1 hari.");
        }
        double total = kendaraan.hitungSewa(lamaSewa, diskonPersen);
        if (asuransiTambahan) {
            total += TransaksiSewa.BIAYA_ASURANSI_PER_HARI * lamaSewa;
        }
        return total;
    }

    /* ==================== DASHBOARD ==================== */

    /** Mengambil seluruh angka ringkasan untuk halaman dashboard. */
    public StatistikDashboard ambilStatistik() throws SQLException {
        int totalKendaraan = kendaraanDAO.hitungTotal();
        int tersedia = kendaraanDAO.hitungTersedia();
        int disewa = kendaraanDAO.hitungDisewa();
        int aktif = transaksiDAO.hitungAktif();
        int selesai = transaksiDAO.hitungSelesai();
        double pendapatan = transaksiDAO.totalPendapatan();

        return new StatistikDashboard(totalKendaraan, tersedia, disewa,
                aktif, selesai, pendapatan);
    }
}
