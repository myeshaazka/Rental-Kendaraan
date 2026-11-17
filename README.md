# Sistem Manajemen Rental Kendaraan

Aplikasi desktop **Java Swing** untuk mengelola rental kendaraan (Mobil, Motor, Bus),
terhubung ke **PostgreSQL Supabase** melalui **JDBC**. Proyek memakai arsitektur
**MVC + DAO**, sudah dilengkapi driver PostgreSQL, konfigurasi Eclipse, dan
`schema.sql` siap dijalankan di SQL Editor Supabase.

---

## 1. Fitur Aplikasi

| Halaman | Fitur |
|---|---|
| **Login** | Login akun lokal (`admin` / `admin123`) + tombol **Tes Koneksi Database** |
| **Dashboard** | Ringkasan total armada, jumlah tersedia, sedang disewa, total pendapatan, transaksi aktif/selesai, dan tabel kendaraan yang sedang disewa |
| **Manajemen Kendaraan** | CRUD penuh Mobil / Motor / Bus, pencarian, sinkron ke tabel `kendaraan` |
| **Penyewa & Transaksi Sewa** | Tambah transaksi sewa (data penyewa + kendaraan + durasi + metode bayar + asuransi), estimasi biaya otomatis, proses pengembalian (kendaraan kembali `Tersedia`), hapus riwayat transaksi |

Komponen input yang dipakai: `JTextField`, `JComboBox`, `JRadioButton`,
`JCheckBox`, `JSpinner`, `JTable`, `JMenuBar`, `CardLayout`,
`JOptionPane.showConfirmDialog` / `showMessageDialog`.

---

## 2. Struktur Proyek

```
rentalkendaraan/
├── .classpath                     <- sudah berisi referensi driver PostgreSQL
├── .project                       <- metadata proyek Eclipse
├── .settings/
│   ├── org.eclipse.core.resources.prefs
│   └── org.eclipse.jdt.core.prefs
├── lib/
│   └── postgresql-42.7.4.jar      <- driver JDBC PostgreSQL
├── schema.sql                     <- jalankan di SQL Editor Supabase
├── run.ps1                        <- compile + run cepat via PowerShell
├── README.md
└── src/
    ├── db.properties              <- konfigurasi koneksi Supabase
    ├── com/rentalkendaraan/
    │   ├── Main.java
    │   ├── config/
    │   │   └── DatabaseConnection.java
    │   ├── exception/
    │   │   ├── InputTidakValidException.java
    │   │   ├── KendaraanTidakTersediaException.java
    │   │   └── DataTidakDitemukanException.java
    │   ├── model/
    │   │   ├── DapatDisewa.java
    │   │   ├── Kendaraan.java
    │   │   ├── Mobil.java
    │   │   ├── Motor.java
    │   │   ├── Bus.java
    │   │   ├── Penyewa.java
    │   │   ├── TransaksiSewa.java
    │   │   └── StatistikDashboard.java
    │   ├── dao/
    │   │   ├── KendaraanDAO.java
    │   │   └── TransaksiDAO.java
    │   ├── controller/
    │   │   └── RentalController.java
    │   └── view/
    │       ├── MainFrame.java
    │       ├── LoginPanel.java
    │       ├── DashboardPanel.java
    │       ├── KendaraanPanel.java
    │       ├── TransaksiPanel.java
    │       ├── KendaraanTableModel.java
    │       ├── TransaksiTableModel.java
    │       └── UiHelper.java
    └── ...
```

---

## 3. Setup Database Supabase

1. Buat proyek baru di [supabase.com](https://supabase.com).
2. Buka **SQL Editor** → **New query**.
3. Copy seluruh isi `schema.sql`, tempel, lalu klik **Run**.
   Perintah ini membuat tabel `kendaraan`, `penyewa`, `transaksi` beserta
   data dummy (8 kendaraan, 3 penyewa, 3 transaksi).
   > **Peringatan:** `schema.sql` memakai `DROP TABLE`, jadi menjalankannya ulang
   > akan menghapus data yang sudah ada.
4. Buka **Project Settings → Database → Connection Pooling / Session Pooler**.
   Salin:
   - **Host** (contoh `aws-0-ap-southeast-1.pooler.supabase.com`)
   - **Port** `5432` (Session Pooler)
   - **User** (contoh `postgres.abcdefghijklmnop`)
   - **Password** database Anda
5. Buka file `src/db.properties`, isi ketiga nilai berikut:

   ```properties
   db.url=jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:5432/postgres?sslmode=require
   db.user=postgres.abcdefghijklmnop
   db.password=passwordDatabaseAnda
   ```

   Placeholder `[REGION]`, `[PROJECT-REF]`, dan `[PASSWORD-ANDA]` **wajib diganti**.
   Aplikasi akan menolak koneksi dan menampilkan pesan yang jelas bila masih
   ada placeholder.

> Kebutuhan minimum: **JDK 11 atau lebih baru** (proyek di-set ke compliance
> Java 11 agar kompatibel dengan Eclipse versi mana pun).

---

## 4. Import Proyek ke Eclipse

### Cara A — Existing Projects (paling cepat)

1. Buka Eclipse (Java EE / Enterprise atau Java Developer).
2. **File → Import → General → Existing Projects into Workspace**.
3. **Select root directory** → arahkan ke folder `rentalkendaraan`
   (folder yang berisi `.project`).
4. Klik **Finish**. Proyek akan otomatis ter-compile karena `.classpath`
   sudah menunjuk ke `src`, JRE, dan `lib/postgresql-42.7.4.jar`.

### Cara B — Membuat proyek baru manual

1. **File → New → Java Project**, beri nama `rentalkendaraan`.
2. Hilangkan centang **Use default location**, arahkan ke folder proyek ini.
3. Klik **Next → Finish**.
4. Klik kanan proyek → **Build Path → Configure Build Path → Libraries**.
5. Tambahkan driver JDBC (lihat langkah 5).

---

## 5. Menambahkan Driver `postgresql-*.jar` ke Build Path Eclipse

Driver sudah disertakan di `lib/postgresql-42.7.4.jar` dan `.classpath` sudah
mereferensikannya. Bila Eclipse belum mengenalinya (misalnya karena proyek
di-import dengan cara B atau memakai JAR versi lain), lakukan salah satu cara:

**Cara 1 — Add JARs (JAR di dalam proyek, disarankan)**

1. Klik kanan proyek → **Build Path → Configure Build Path…**
2. Tab **Libraries** → tombol **Add JARs…**
3. Pilih `rentalkendaraan/lib/postgresql-42.7.4.jar` → **OK**.
4. Tab **Order and Export** → centang JAR tersebut agar ikut saat run.
5. **Apply and Close**, lalu **Project → Clean…** untuk memastikan
   `db.properties` ikut ter-copy ke folder `bin`.

**Cara 2 — Add External JARs (JAR di luar proyek)**

1. **Build Path → Configure Build Path… → Libraries → Add External JARs…**
2. Pilih file `postgresql-42.7.4.jar` dari folder mana pun → **Open**.
3. **Apply and Close**.

**Cara 3 — Maven (opsional)**

Tambahkan dependency berikut pada `pom.xml`:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.4</version>
</dependency>
```

> Tanpa driver ini, aplikasi menampilkan pesan:
> *"Driver PostgreSQL tidak ditemukan. Tambahkan file postgresql-42.7.4.jar ke Build Path Eclipse…"*

---

## 6. Menjalankan Aplikasi

1. Pastikan proyek sudah **Project → Build Automatically** (atau tekan `Ctrl+B`).
2. Buka `src/com/rentalkendaraan/Main.java`.
3. Klik kanan → **Run As → Java Application**.
4. Di halaman login:
   - Username: `admin`
   - Password: `admin123`
   - Klik **Tes Koneksi Database** untuk memastikan Supabase terhubung.
5. Setelah login, gunakan menu **Aplikasi / Data** atau tombol navigasi untuk
   berpindah halaman.

### Menjalankan tanpa Eclipse (opsional)

PowerShell dari folder proyek:

```powershell
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

atau manual:

```powershell
javac -encoding UTF-8 -d bin -cp "lib\postgresql-42.7.4.jar" (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp "bin;lib\postgresql-42.7.4.jar" com.rentalkendaraan.Main
```

---

## 7. Penerapan Konsep OOP (untuk laporan)

| Konsep | Implementasi |
|---|---|
| **Abstraksi** | `abstract class Kendaraan` dengan `hitungSewa(int)` dan `getDetailSpesifik()` abstrak; `interface DapatDisewa` |
| **Pewarisan** | `Mobil`, `Motor`, `Bus` extends `Kendaraan`; semua konstruktor memanggil `super(...)` |
| **Enkapsulasi** | Atribut `private`/`protected` + getter/setter; validasi di `setTarifHarian`, `setPlatNomor`, `setLamaSewa`, `setNomorTelepon`, dll. |
| **Polimorfisme — Overriding** | `hitungSewa(int)` berbeda di `Mobil` (tambah biaya sopir), `Motor` (diskon 10% ≥ 3 hari), `Bus` (biaya kru/asuransi) |
| **Polimorfisme — Overloading** | `Kendaraan.hitungSewa(int, double)` dan `TransaksiSewa.hitungTotalBiaya(int, double)` |
| **Interface** | `DapatDisewa` diimplementasikan oleh `Kendaraan` dan `TransaksiSewa` |
| **Collection & Generics** | `List<Kendaraan>`, `List<TransaksiSewa>`, `Map<String, Kendaraan>` di DAO/controller |
| **Exception** | `InputTidakValidException`, `KendaraanTidakTersediaException`, `DataTidakDitemukanException`, plus penanganan `SQLException` |
| **MVC + DAO** | Model (`model`), View (`view`), Controller (`controller`), akses data (`dao`) |
| **JDBC aman** | Semua query memakai `PreparedStatement` + try-with-resources; operasi multi-tabel memakai transaksi `commit`/`rollback` |

### Aturan perhitungan biaya

| Jenis | Rumus |
|---|---|
| Mobil | `tarif × hari + (150.000 × hari bila Dengan Sopir)` |
| Motor | `tarif × hari`, diskon **10%** bila `hari ≥ 3` |
| Bus | `tarif × hari + 250.000` (kru & asuransi) |
| Asuransi tambahan (opsional) | `+ 25.000 × hari` |
| Overloading diskon | `hitungSewa(hari, diskonPersen)` mengurangi total sesuai persentase |

---

## 8. Troubleshooting

| Gejala | Penyebab & solusi |
|---|---|
| *Driver PostgreSQL tidak ditemukan* | JAR belum masuk Build Path. Ikuti langkah 5, lalu **Project → Clean**. |
| *Konfigurasi database belum lengkap* | Placeholder di `src/db.properties` belum diganti. |
| *The connection attempt failed* / timeout | Region/host pooler salah, atau jaringan memblokir port 5432. Salin ulang connection string Session Pooler dari Supabase. |
| *password authentication failed* | Password salah. Reset password di **Project Settings → Database**. |
| *Tenant or user not found* | Username pooler salah; gunakan format `postgres.[PROJECT-REF]` dari halaman Connection Pooling. |
| *relation "kendaraan" does not exist* | `schema.sql` belum dijalankan di SQL Editor Supabase. |
| Perubahan `db.properties` tidak terbaca | File harus ada di `src/` agar ter-copy ke `bin/`. Lakukan **Project → Clean** lalu jalankan ulang. |
| Tabel kosong padahal data ada | Pastikan login memakai proyek Supabase yang benar dan `schema.sql` dijalankan di proyek tersebut. |

---

## 9. Catatan Keamanan

- Login `admin/admin123` disimpan di kode untuk kebutuhan demonstrasi tugas;
  untuk produksi gunakan tabel pengguna + hash password (mis. BCrypt).
- Jangan commit `db.properties` berisi password asli ke repositori publik.
- Aplikasi memakai Session Pooler (port 5432) sesuai rekomendasi Supabase
  untuk koneksi JDBC persisten.
