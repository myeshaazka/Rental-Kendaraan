-- ============================================================================
--  SISTEM MANAJEMEN RENTAL KENDARAAN
--  Skema database PostgreSQL (Supabase)
--
--  CARA PAKAI:
--  1. Buka dashboard Supabase > SQL Editor > New query.
--  2. Copy seluruh isi file ini, lalu klik RUN.
--  3. File ini akan membuat tabel kendaraan, penyewa, transaksi beserta
--     data dummy awal. PERINGATAN: DROP TABLE akan menghapus data lama.
-- ============================================================================

-- Hapus tabel lama (urutan mengikuti relasi foreign key).
DROP TABLE IF EXISTS transaksi;
DROP TABLE IF EXISTS penyewa;
DROP TABLE IF EXISTS kendaraan;

-- ============================================================================
-- 1. TABEL KENDARAAN
--    Satu tabel untuk ketiga subclass (single table inheritance):
--    kolom khusus Mobil / Motor / Bus boleh NULL.
-- ============================================================================
CREATE TABLE kendaraan (
    kode_kendaraan VARCHAR(10)  PRIMARY KEY,
    jenis          VARCHAR(10)  NOT NULL CHECK (jenis IN ('MOBIL', 'MOTOR', 'BUS')),
    merk           VARCHAR(60)  NOT NULL,
    plat_nomor     VARCHAR(15)  NOT NULL UNIQUE,
    tarif_harian   NUMERIC(12,2) NOT NULL CHECK (tarif_harian > 0),
    tersedia       BOOLEAN      NOT NULL DEFAULT TRUE,
    dengan_sopir   BOOLEAN,
    kapasitas_cc   INTEGER      CHECK (kapasitas_cc IS NULL OR kapasitas_cc > 0),
    jumlah_kursi   INTEGER      CHECK (jumlah_kursi IS NULL OR jumlah_kursi > 0),

    CONSTRAINT chk_atribut_mobil CHECK (
        jenis <> 'MOBIL' OR (dengan_sopir IS NOT NULL
                             AND kapasitas_cc IS NULL
                             AND jumlah_kursi IS NULL)
    ),
    CONSTRAINT chk_atribut_motor CHECK (
        jenis <> 'MOTOR' OR (kapasitas_cc IS NOT NULL
                             AND dengan_sopir IS NULL
                             AND jumlah_kursi IS NULL)
    ),
    CONSTRAINT chk_atribut_bus CHECK (
        jenis <> 'BUS' OR (jumlah_kursi IS NOT NULL
                           AND dengan_sopir IS NULL
                           AND kapasitas_cc IS NULL)
    )
);

-- ============================================================================
-- 2. TABEL PENYEWA
-- ============================================================================
CREATE TABLE penyewa (
    id_penyewa    SERIAL       PRIMARY KEY,
    nama          VARCHAR(80)  NOT NULL,
    nomor_ktp     VARCHAR(16)  NOT NULL UNIQUE,
    nomor_telepon VARCHAR(20)  NOT NULL,
    alamat        VARCHAR(200)
);

-- ============================================================================
-- 3. TABEL TRANSAKSI SEWA
-- ============================================================================
CREATE TABLE transaksi (
    id_transaksi      SERIAL        PRIMARY KEY,
    kode_kendaraan    VARCHAR(10)   NOT NULL
                      REFERENCES kendaraan (kode_kendaraan) ON UPDATE CASCADE,
    id_penyewa        INTEGER       NOT NULL
                      REFERENCES penyewa (id_penyewa),
    lama_sewa         INTEGER       NOT NULL CHECK (lama_sewa > 0),
    tanggal_sewa      DATE          NOT NULL,
    tanggal_kembali   DATE,
    total_biaya       NUMERIC(14,2) NOT NULL CHECK (total_biaya >= 0),
    status            VARCHAR(10)   NOT NULL DEFAULT 'AKTIF'
                      CHECK (status IN ('AKTIF', 'SELESAI')),
    metode_pembayaran VARCHAR(20)   NOT NULL,
    asuransi_tambahan BOOLEAN       NOT NULL DEFAULT FALSE,

    CONSTRAINT chk_tanggal CHECK (
        tanggal_kembali IS NULL OR tanggal_kembali >= tanggal_sewa
    )
);

CREATE INDEX idx_transaksi_kendaraan ON transaksi (kode_kendaraan);
CREATE INDEX idx_transaksi_penyewa   ON transaksi (id_penyewa);
CREATE INDEX idx_transaksi_status    ON transaksi (status);

-- ============================================================================
-- 4. DATA DUMMY KENDARAAN (8 unit)
-- ============================================================================
INSERT INTO kendaraan
    (kode_kendaraan, jenis, merk, plat_nomor, tarif_harian, tersedia,
     dengan_sopir, kapasitas_cc, jumlah_kursi)
VALUES
    ('MBL-001', 'MOBIL', 'Toyota Avanza',        'B 1234 XYZ', 350000.00, FALSE, TRUE,  NULL, NULL),
    ('MBL-002', 'MOBIL', 'Honda CR-V',           'B 5678 ABC', 550000.00, TRUE,  FALSE, NULL, NULL),
    ('MBL-003', 'MOBIL', 'Suzuki Ertiga',        'D 9012 DEF', 400000.00, TRUE,  TRUE,  NULL, NULL),
    ('MTR-001', 'MOTOR', 'Honda Vario 160',      'B 2345 VRA',  90000.00, TRUE,  NULL, 160,  NULL),
    ('MTR-002', 'MOTOR', 'Yamaha NMAX',          'B 3456 NMX', 120000.00, TRUE,  NULL, 155,  NULL),
    ('MTR-003', 'MOTOR', 'Honda Beat',           'B 4567 BEA',  75000.00, TRUE,  NULL, 110,  NULL),
    ('BUS-001', 'BUS',   'Hino RK8',             'B 7890 BUS', 1500000.00, TRUE, NULL, NULL, 45),
    ('BUS-002', 'BUS',   'Mercedes-Benz OH 1526', 'D 8901 MBZ', 2200000.00, TRUE, NULL, NULL, 50);

-- ============================================================================
-- 5. DATA DUMMY PENYEWA (3 orang)
-- ============================================================================
INSERT INTO penyewa (nama, nomor_ktp, nomor_telepon, alamat)
VALUES
    ('Budi Santoso', '3201010101900001', '081234567890', 'Jl. Merdeka No. 10, Bogor'),
    ('Siti Aminah',  '3201010202920002', '081298765432', 'Jl. Pahlawan No. 25, Depok'),
    ('Andi Wijaya',  '3201010303930003', '085612345678', 'Jl. Sudirman No. 7, Jakarta');

-- ============================================================================
-- 6. DATA DUMMY TRANSAKSI
--    MBL-001 sedang disewa (AKTIF)  -> total 3 hari x (350.000 + sopir 150.000)
--    MTR-002 sudah dikembalikan     -> 4 hari x 120.000, diskon 10% = 432.000
--    BUS-001 sudah dikembalikan     -> 2 x 1.500.000 + kru/asuransi 250.000
--                                      + asuransi tambahan 2 x 25.000 = 3.300.000
-- ============================================================================
INSERT INTO transaksi
    (kode_kendaraan, id_penyewa, lama_sewa, tanggal_sewa, tanggal_kembali,
     total_biaya, status, metode_pembayaran, asuransi_tambahan)
VALUES
    ('MBL-001',
     (SELECT id_penyewa FROM penyewa WHERE nomor_ktp = '3201010101900001'),
     3, CURRENT_DATE - 1, NULL, 1500000.00, 'AKTIF', 'Transfer Bank', FALSE),

    ('MTR-002',
     (SELECT id_penyewa FROM penyewa WHERE nomor_ktp = '3201010202920002'),
     4, CURRENT_DATE - 6, CURRENT_DATE - 2, 432000.00, 'SELESAI', 'Tunai', FALSE),

    ('BUS-001',
     (SELECT id_penyewa FROM penyewa WHERE nomor_ktp = '3201010303930003'),
     2, CURRENT_DATE - 10, CURRENT_DATE - 8, 3300000.00, 'SELESAI', 'Transfer Bank', TRUE);

-- ============================================================================
-- 7. VERIFIKASI (opsional, jalankan manual untuk memastikan data masuk)
-- ============================================================================
-- SELECT * FROM kendaraan ORDER BY kode_kendaraan;
-- SELECT * FROM penyewa ORDER BY id_penyewa;
-- SELECT t.id_transaksi, t.kode_kendaraan, p.nama, t.lama_sewa, t.total_biaya,
--        t.status, t.tanggal_sewa, t.tanggal_kembali
-- FROM transaksi t JOIN penyewa p ON p.id_penyewa = t.id_penyewa
-- ORDER BY t.id_transaksi;
