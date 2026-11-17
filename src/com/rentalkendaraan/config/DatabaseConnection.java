package com.rentalkendaraan.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Konfigurasi koneksi JDBC ke database PostgreSQL Supabase.
 *
 * <p>Nilai koneksi dibaca dari file {@code db.properties} yang berada di
 * folder {@code src} (sehingga otomatis ikut ter-copy ke folder {@code bin}
 * oleh Eclipse dan tersedia di classpath). Bila file tidak ditemukan atau
 * atributnya kosong, maka nilai default (placeholder) di bawah ini yang
 * dipakai.</p>
 *
 * <p><b>PENTING:</b> ganti placeholder {@code [REGION]}, {@code [PROJECT-REF]},
 * dan {@code [PASSWORD-ANDA]} dengan data dari dashboard Supabase
 * (Project Settings - Database - Connection Pooling / Session Pooler).</p>
 */
public final class DatabaseConnection {

    /** Placeholder URL Session Pooler Supabase. */
    public static final String DEFAULT_URL =
            "jdbc:postgresql://aws-0-[REGION].pooler.supabase.com:5432/postgres?sslmode=require";

    /** Placeholder username pooler Supabase (biasanya postgres.[PROJECT-REF]). */
    public static final String DEFAULT_USER = "postgres.[PROJECT-REF]";

    /** Placeholder password database. */
    public static final String DEFAULT_PASSWORD = "[PASSWORD-ANDA]";

    private static final String NAMA_FILE_KONFIGURASI = "db.properties";
    private static final Properties KONFIGURASI = new Properties();

    private static boolean driverDimuat = false;

    static {
        muatKonfigurasi();
    }

    private DatabaseConnection() {
        // Utility class, tidak boleh diinstansiasi.
    }

    /**
     * Membaca db.properties dari classpath (folder bin) terlebih dahulu,
     * kemudian dari working directory sebagai fallback.
     */
    private static void muatKonfigurasi() {
        try (InputStream input = DatabaseConnection.class
                .getResourceAsStream("/" + NAMA_FILE_KONFIGURASI)) {
            if (input != null) {
                KONFIGURASI.load(input);
            }
        } catch (IOException e) {
            System.err.println("[DatabaseConnection] Gagal membaca " + NAMA_FILE_KONFIGURASI
                    + " dari classpath: " + e.getMessage());
        }

        if (KONFIGURASI.isEmpty()) {
            bacaDariFile(new File(NAMA_FILE_KONFIGURASI));
        }
        if (KONFIGURASI.isEmpty()) {
            bacaDariFile(new File("src", NAMA_FILE_KONFIGURASI));
        }
    }

    private static void bacaDariFile(File file) {
        if (!file.isFile()) {
            return;
        }
        try (InputStream input = new FileInputStream(file)) {
            KONFIGURASI.load(input);
        } catch (IOException e) {
            System.err.println("[DatabaseConnection] Gagal membaca " + file.getPath()
                    + ": " + e.getMessage());
        }
    }

    private static String ambil(String kunci, String nilaiDefault) {
        String nilai = KONFIGURASI.getProperty(kunci);
        if (nilai == null) {
            return nilaiDefault;
        }
        return nilai.trim();
    }

    public static String getUrl() {
        return ambil("db.url", DEFAULT_URL);
    }

    public static String getUser() {
        return ambil("db.user", DEFAULT_USER);
    }

    public static String getPassword() {
        return ambil("db.password", DEFAULT_PASSWORD);
    }

    /** Info koneksi yang aman ditampilkan (tanpa password). */
    public static String getInfoKoneksi() {
        return "URL  : " + getUrl() + System.lineSeparator()
                + "User : " + getUser();
    }

    private static void muatDriver() throws SQLException {
        if (driverDimuat) {
            return;
        }
        try {
            Class.forName("org.postgresql.Driver");
            driverDimuat = true;
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "Driver PostgreSQL tidak ditemukan. Tambahkan file "
                            + "postgresql-42.7.4.jar ke Build Path Eclipse "
                            + "(klik kanan proyek > Build Path > Configure Build Path > "
                            + "Libraries > Add JARs).", e);
        }
    }

    /**
     * Membuka koneksi baru ke database Supabase.
     *
     * @return koneksi JDBC yang siap dipakai (pemanggil wajib menutupnya,
     *         disarankan dengan try-with-resources)
     * @throws SQLException bila konfigurasi belum diisi, driver tidak ada,
     *         atau koneksi gagal dibuat
     */
    public static Connection getConnection() throws SQLException {
        String url = getUrl();
        String user = getUser();
        String password = getPassword();

        if (url.isEmpty() || user.isEmpty()) {
            throw new SQLException(
                    "db.url dan db.user pada file db.properties belum diisi. "
                            + "Salin connection string Session Pooler dari dashboard Supabase "
                            + "(Project Settings > Database > Connection Pooling).");
        }

        if (url.contains("[REGION]") || user.contains("[PROJECT-REF]")
                || password.contains("[PASSWORD-ANDA]")) {
            throw new SQLException(
                    "Konfigurasi database belum lengkap. Buka file src/db.properties "
                            + "lalu isi db.url, db.user, dan db.password dengan data "
                            + "Session Pooler dari dashboard Supabase "
                            + "(Project Settings > Database > Connection Pooling).");
        }

        muatDriver();
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Menutup satu atau beberapa resource JDBC secara aman tanpa melempar
     * exception baru. Method ini opsional karena DAO sudah memakai
     * try-with-resources.
     */
    public static void tutup(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    System.err.println("[DatabaseConnection] Gagal menutup resource: "
                            + e.getMessage());
                }
            }
        }
    }
}
