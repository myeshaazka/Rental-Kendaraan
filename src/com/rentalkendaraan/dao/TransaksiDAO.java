package com.rentalkendaraan.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.rentalkendaraan.config.DatabaseConnection;
import com.rentalkendaraan.model.Kendaraan;
import com.rentalkendaraan.model.Penyewa;
import com.rentalkendaraan.model.TransaksiSewa;

/**
 * Data Access Object untuk tabel {@code transaksi} dan {@code penyewa}.
 *
 * <p>Operasi yang menyentuh lebih dari satu tabel (simpan sewa, proses
 * pengembalian, hapus riwayat) dijalankan dalam satu transaksi database
 * (commit/rollback) agar data tetap konsisten.</p>
 */
public class TransaksiDAO {

    private static final String SQL_SELECT_DASAR =
            "SELECT t.id_transaksi, t.kode_kendaraan, t.lama_sewa, t.tanggal_sewa, "
                    + "t.tanggal_kembali, t.total_biaya, t.status, t.metode_pembayaran, "
                    + "t.asuransi_tambahan, p.id_penyewa AS penyewa_id, "
                    + "p.nama AS penyewa_nama, p.nomor_ktp AS penyewa_ktp, "
                    + "p.nomor_telepon AS penyewa_telepon, p.alamat AS penyewa_alamat "
                    + "FROM transaksi t JOIN penyewa p ON p.id_penyewa = t.id_penyewa";

    private static final String SQL_FIND_ALL =
            SQL_SELECT_DASAR + " ORDER BY t.id_transaksi DESC";

    private static final String SQL_FIND_BY_ID =
            SQL_SELECT_DASAR + " WHERE t.id_transaksi = ?";

    /** Mengambil semua transaksi (terbaru lebih dulu) beserta kendaraan & penyewa. */
    public List<TransaksiSewa> findAll() throws SQLException {
        Map<String, Kendaraan> petaKendaraan = new KendaraanDAO().findAllAsMap();
        List<TransaksiSewa> hasil = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                hasil.add(mapRow(rs, petaKendaraan));
            }
        }
        return hasil;
    }

    /** @return satu transaksi berdasarkan ID, atau {@code null} bila tidak ada. */
    public TransaksiSewa findById(int idTransaksi) throws SQLException {
        Map<String, Kendaraan> petaKendaraan = new KendaraanDAO().findAllAsMap();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setInt(1, idTransaksi);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs, petaKendaraan);
                }
            }
        }
        return null;
    }

    /**
     * Menyimpan transaksi sewa baru secara atomik:
     * <ol>
     *   <li>menyimpan / memperbarui data penyewa (upsert berdasarkan nomor KTP),</li>
     *   <li>menyisipkan baris transaksi,</li>
     *   <li>menandai kendaraan menjadi tidak tersedia.</li>
     * </ol>
     * Bila salah satu langkah gagal, seluruh perubahan di-rollback.
     */
    public void simpanSewaBaru(TransaksiSewa transaksi) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                int idPenyewa = simpanPenyewa(conn, transaksi.getPenyewa());
                transaksi.getPenyewa().setIdPenyewa(idPenyewa);

                String sql = "INSERT INTO transaksi (kode_kendaraan, id_penyewa, lama_sewa, "
                        + "tanggal_sewa, tanggal_kembali, total_biaya, status, "
                        + "metode_pembayaran, asuransi_tambahan) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement ps = conn.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, transaksi.getKendaraan().getKodeKendaraan());
                    ps.setInt(2, idPenyewa);
                    ps.setInt(3, transaksi.getLamaSewa());
                    ps.setDate(4, java.sql.Date.valueOf(transaksi.getTanggalSewa()));
                    if (transaksi.getTanggalKembali() == null) {
                        ps.setNull(5, Types.DATE);
                    } else {
                        ps.setDate(5, java.sql.Date.valueOf(transaksi.getTanggalKembali()));
                    }
                    ps.setDouble(6, transaksi.getTotalBiaya());
                    ps.setString(7, transaksi.getStatus());
                    ps.setString(8, transaksi.getMetodePembayaran());
                    ps.setBoolean(9, transaksi.isAsuransiTambahan());
                    ps.executeUpdate();

                    try (ResultSet kunci = ps.getGeneratedKeys()) {
                        if (kunci.next()) {
                            transaksi.setIdTransaksi(kunci.getInt(1));
                        }
                    }
                }

                ubahKetersediaan(conn, transaksi.getKendaraan().getKodeKendaraan(), false);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } catch (RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        }
    }

    /**
     * Memproses pengembalian kendaraan: memperbarui tanggal kembali + status
     * transaksi, lalu menandai kendaraan kembali tersedia. Atomik.
     */
    public void prosesPengembalian(TransaksiSewa transaksi) throws SQLException {
        if (transaksi.getTanggalKembali() == null) {
            throw new SQLException("Tanggal kembali belum diisi. "
                    + "Panggil prosesKembali() pada objek transaksi terlebih dahulu.");
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                String sql = "UPDATE transaksi SET tanggal_kembali = ?, status = ? "
                        + "WHERE id_transaksi = ? AND status = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setDate(1, java.sql.Date.valueOf(transaksi.getTanggalKembali()));
                    ps.setString(2, TransaksiSewa.STATUS_SELESAI);
                    ps.setInt(3, transaksi.getIdTransaksi());
                    ps.setString(4, TransaksiSewa.STATUS_AKTIF);
                    int baris = ps.executeUpdate();
                    if (baris == 0) {
                        throw new SQLException("Transaksi tidak ditemukan atau sudah "
                                + "berstatus SELESAI di database.");
                    }
                }

                ubahKetersediaan(conn, transaksi.getKendaraan().getKodeKendaraan(), true);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        }
    }

    /**
     * Menghapus riwayat transaksi. Bila transaksi yang dihapus masih AKTIF,
     * kendaraannya otomatis dikembalikan ke status tersedia. Atomik.
     */
    public void hapus(int idTransaksi) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                String kodeKendaraan = null;
                String status = null;

                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT kode_kendaraan, status FROM transaksi WHERE id_transaksi = ?")) {
                    ps.setInt(1, idTransaksi);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            kodeKendaraan = rs.getString("kode_kendaraan");
                            status = rs.getString("status");
                        }
                    }
                }

                if (kodeKendaraan == null) {
                    throw new SQLException("Transaksi dengan ID " + idTransaksi
                            + " tidak ditemukan di database.");
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM transaksi WHERE id_transaksi = ?")) {
                    ps.setInt(1, idTransaksi);
                    ps.executeUpdate();
                }

                if (TransaksiSewa.STATUS_AKTIF.equals(status)) {
                    ubahKetersediaan(conn, kodeKendaraan, true);
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        }
    }

    /** Total pendapatan dari seluruh transaksi yang tercatat. */
    public double totalPendapatan() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COALESCE(SUM(total_biaya), 0) FROM transaksi");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    public int hitungAktif() throws SQLException {
        return hitung("SELECT COUNT(*) FROM transaksi WHERE status = 'AKTIF'");
    }

    public int hitungSelesai() throws SQLException {
        return hitung("SELECT COUNT(*) FROM transaksi WHERE status = 'SELESAI'");
    }

    private int hitung(String sql) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Upsert penyewa berdasarkan nomor KTP (unik), lalu mengembalikan
     * id_penyewa yang dihasilkan/ditemukan.
     */
    private int simpanPenyewa(Connection conn, Penyewa penyewa) throws SQLException {
        String sql = "INSERT INTO penyewa (nama, nomor_ktp, nomor_telepon, alamat) "
                + "VALUES (?, ?, ?, ?) "
                + "ON CONFLICT (nomor_ktp) DO UPDATE SET "
                + "nama = EXCLUDED.nama, nomor_telepon = EXCLUDED.nomor_telepon, "
                + "alamat = EXCLUDED.alamat "
                + "RETURNING id_penyewa";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, penyewa.getNama());
            ps.setString(2, penyewa.getNomorKtp());
            ps.setString(3, penyewa.getNomorTelepon());
            ps.setString(4, penyewa.getAlamat());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Gagal menyimpan data penyewa ke database.");
    }

    private void ubahKetersediaan(Connection conn, String kodeKendaraan, boolean tersedia)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE kendaraan SET tersedia = ? WHERE kode_kendaraan = ?")) {
            ps.setBoolean(1, tersedia);
            ps.setString(2, kodeKendaraan);
            ps.executeUpdate();
        }
    }

    private TransaksiSewa mapRow(ResultSet rs, Map<String, Kendaraan> petaKendaraan)
            throws SQLException {
        String kode = rs.getString("kode_kendaraan");
        Kendaraan kendaraan = petaKendaraan.get(kode);
        if (kendaraan == null) {
            throw new SQLException("Data kendaraan dengan kode " + kode
                    + " tidak ditemukan untuk transaksi "
                    + rs.getInt("id_transaksi") + ".");
        }

        Penyewa penyewa = new Penyewa(
                rs.getInt("penyewa_id"),
                rs.getString("penyewa_nama"),
                rs.getString("penyewa_ktp"),
                rs.getString("penyewa_telepon"),
                rs.getString("penyewa_alamat"));

        java.sql.Date tanggalSewa = rs.getDate("tanggal_sewa");
        java.sql.Date tanggalKembali = rs.getDate("tanggal_kembali");

        LocalDate sewa = (tanggalSewa == null) ? null : tanggalSewa.toLocalDate();
        LocalDate kembali = (tanggalKembali == null) ? null : tanggalKembali.toLocalDate();

        return new TransaksiSewa(
                rs.getInt("id_transaksi"),
                kendaraan,
                penyewa,
                rs.getInt("lama_sewa"),
                sewa,
                kembali,
                rs.getDouble("total_biaya"),
                rs.getString("status"),
                rs.getString("metode_pembayaran"),
                rs.getBoolean("asuransi_tambahan"));
    }
}
