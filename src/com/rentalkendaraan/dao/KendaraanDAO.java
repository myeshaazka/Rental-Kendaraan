package com.rentalkendaraan.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.rentalkendaraan.config.DatabaseConnection;
import com.rentalkendaraan.model.Bus;
import com.rentalkendaraan.model.Kendaraan;
import com.rentalkendaraan.model.Mobil;
import com.rentalkendaraan.model.Motor;

/**
 * Data Access Object untuk tabel {@code kendaraan}.
 *
 * <p>Seluruh query memakai {@link PreparedStatement} (aman dari SQL Injection)
 * dan setiap resource JDBC ditutup dengan try-with-resources.</p>
 */
public class KendaraanDAO {

    private static final String KOLOM =
            "kode_kendaraan, jenis, merk, plat_nomor, tarif_harian, tersedia, "
                    + "dengan_sopir, kapasitas_cc, jumlah_kursi";

    private static final String SQL_FIND_ALL =
            "SELECT " + KOLOM + " FROM kendaraan ORDER BY kode_kendaraan";

    private static final String SQL_FIND_TERSEDIA =
            "SELECT " + KOLOM + " FROM kendaraan WHERE tersedia = TRUE ORDER BY kode_kendaraan";

    private static final String SQL_FIND_BY_KODE =
            "SELECT " + KOLOM + " FROM kendaraan WHERE kode_kendaraan = ?";

    private static final String SQL_INSERT =
            "INSERT INTO kendaraan (" + KOLOM + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE kendaraan SET jenis = ?, merk = ?, plat_nomor = ?, tarif_harian = ?, "
                    + "tersedia = ?, dengan_sopir = ?, kapasitas_cc = ?, jumlah_kursi = ? "
                    + "WHERE kode_kendaraan = ?";

    private static final String SQL_DELETE =
            "DELETE FROM kendaraan WHERE kode_kendaraan = ?";

    private static final String SQL_UPDATE_TERSEDIA =
            "UPDATE kendaraan SET tersedia = ? WHERE kode_kendaraan = ?";

    /** @return seluruh kendaraan diurutkan berdasarkan kode. */
    public List<Kendaraan> findAll() throws SQLException {
        List<Kendaraan> hasil = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                hasil.add(mapRow(rs));
            }
        }
        return hasil;
    }

    /**
     * Mengambil seluruh kendaraan dalam bentuk {@link Map} dengan kode
     * kendaraan sebagai key. Dipakai untuk join logis di lapisan controller
     * dan {@link TransaksiDAO}.
     */
    public Map<String, Kendaraan> findAllAsMap() throws SQLException {
        Map<String, Kendaraan> peta = new LinkedHashMap<>();
        for (Kendaraan kendaraan : findAll()) {
            peta.put(kendaraan.getKodeKendaraan(), kendaraan);
        }
        return peta;
    }

    /** @return hanya kendaraan yang statusnya tersedia. */
    public List<Kendaraan> findTersedia() throws SQLException {
        List<Kendaraan> hasil = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_TERSEDIA);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                hasil.add(mapRow(rs));
            }
        }
        return hasil;
    }

    /** @return kendaraan dengan kode tertentu, atau {@code null} bila tidak ada. */
    public Kendaraan findByKode(String kodeKendaraan) throws SQLException {
        if (kodeKendaraan == null || kodeKendaraan.trim().isEmpty()) {
            return null;
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_KODE)) {
            ps.setString(1, kodeKendaraan.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /** Menyimpan kendaraan baru (Mobil/Motor/Bus). */
    public void insert(Kendaraan kendaraan) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
            ps.setString(1, kendaraan.getKodeKendaraan());
            ps.setString(2, kendaraan.getJenis());
            ps.setString(3, kendaraan.getMerk());
            ps.setString(4, kendaraan.getPlatNomor());
            ps.setDouble(5, kendaraan.getTarifHarian());
            ps.setBoolean(6, kendaraan.isTersedia());
            bindAtributKhusus(ps, kendaraan, 7);
            ps.executeUpdate();
        }
    }

    /** Memperbarui data kendaraan berdasarkan primary key {@code kode_kendaraan}. */
    public void update(Kendaraan kendaraan) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
            ps.setString(1, kendaraan.getJenis());
            ps.setString(2, kendaraan.getMerk());
            ps.setString(3, kendaraan.getPlatNomor());
            ps.setDouble(4, kendaraan.getTarifHarian());
            ps.setBoolean(5, kendaraan.isTersedia());
            bindAtributKhusus(ps, kendaraan, 6);
            ps.setString(9, kendaraan.getKodeKendaraan());
            ps.executeUpdate();
        }
    }

    /** Menghapus kendaraan berdasarkan kode. */
    public void delete(String kodeKendaraan) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setString(1, kodeKendaraan);
            ps.executeUpdate();
        }
    }

    /** Mengubah status ketersediaan kendaraan (dipakai saat sewa/pengembalian). */
    public void updateKetersediaan(String kodeKendaraan, boolean tersedia) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_TERSEDIA)) {
            ps.setBoolean(1, tersedia);
            ps.setString(2, kodeKendaraan);
            ps.executeUpdate();
        }
    }

    public int hitungTotal() throws SQLException {
        return hitung("SELECT COUNT(*) FROM kendaraan");
    }

    public int hitungTersedia() throws SQLException {
        return hitung("SELECT COUNT(*) FROM kendaraan WHERE tersedia = TRUE");
    }

    public int hitungDisewa() throws SQLException {
        return hitung("SELECT COUNT(*) FROM kendaraan WHERE tersedia = FALSE");
    }

    private int hitung(String sql) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Mengikat kolom atribut khusus sesuai jenis kendaraan. Kolom yang tidak
     * relevan diisi NULL agar konsisten dengan constraint di schema.sql.
     */
    private void bindAtributKhusus(PreparedStatement ps, Kendaraan kendaraan, int mulai)
            throws SQLException {
        if (kendaraan instanceof Mobil) {
            ps.setBoolean(mulai, ((Mobil) kendaraan).isDenganSopir());
            ps.setNull(mulai + 1, Types.INTEGER);
            ps.setNull(mulai + 2, Types.INTEGER);
        } else if (kendaraan instanceof Motor) {
            ps.setNull(mulai, Types.BOOLEAN);
            ps.setInt(mulai + 1, ((Motor) kendaraan).getKapasitasCC());
            ps.setNull(mulai + 2, Types.INTEGER);
        } else if (kendaraan instanceof Bus) {
            ps.setNull(mulai, Types.BOOLEAN);
            ps.setNull(mulai + 1, Types.INTEGER);
            ps.setInt(mulai + 2, ((Bus) kendaraan).getJumlahKursi());
        } else {
            throw new SQLException("Jenis kendaraan tidak dikenali: "
                    + kendaraan.getClass().getSimpleName());
        }
    }

    /** Memetakan satu baris ResultSet menjadi objek subclass yang tepat. */
    private Kendaraan mapRow(ResultSet rs) throws SQLException {
        String kode = rs.getString("kode_kendaraan");
        String jenis = rs.getString("jenis");
        String merk = rs.getString("merk");
        String plat = rs.getString("plat_nomor");
        double tarif = rs.getDouble("tarif_harian");
        boolean tersedia = rs.getBoolean("tersedia");

        switch (jenis) {
            case "MOBIL":
                return new Mobil(kode, merk, plat, tarif, tersedia,
                        rs.getBoolean("dengan_sopir"));
            case "MOTOR":
                return new Motor(kode, merk, plat, tarif, tersedia,
                        rs.getInt("kapasitas_cc"));
            case "BUS":
                return new Bus(kode, merk, plat, tarif, tersedia,
                        rs.getInt("jumlah_kursi"));
            default:
                throw new SQLException("Jenis kendaraan pada database tidak dikenali: " + jenis);
        }
    }
}
