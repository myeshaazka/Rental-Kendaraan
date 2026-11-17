package com.rentalkendaraan.model;

import java.util.Objects;

import com.rentalkendaraan.exception.InputTidakValidException;
import com.rentalkendaraan.exception.KendaraanTidakTersediaException;

/**
 * Kelas abstrak induk untuk seluruh jenis kendaraan.
 *
 * <p>Menerapkan enkapsulasi (atribut {@code protected} + getter/setter
 * bervalidasi), abstraksi ({@link #hitungSewa(int)} dan
 * {@link #getDetailSpesifik()}), pewarisan (lihat {@link Mobil},
 * {@link Motor}, {@link Bus}), serta polimorfisme lewat method overriding
 * dan overloading {@code hitungSewa}.</p>
 */
public abstract class Kendaraan implements DapatDisewa {

    protected String kodeKendaraan;
    protected String merk;
    protected String platNomor;
    protected double tarifHarian;
    protected boolean tersedia;

    protected Kendaraan(String kodeKendaraan, String merk, String platNomor,
                        double tarifHarian, boolean tersedia) {
        setKodeKendaraan(kodeKendaraan);
        setMerk(merk);
        setPlatNomor(platNomor);
        setTarifHarian(tarifHarian);
        this.tersedia = tersedia;
    }

    /* ==================== ABSTRACT METHOD ==================== */

    /**
     * Menghitung biaya sewa sesuai aturan tiap jenis kendaraan.
     *
     * @param hari lama sewa dalam hari
     * @return total biaya sewa
     */
    public abstract double hitungSewa(int hari);

    /**
     * @return deskripsi atribut khusus milik subclass
     *         (misal: sopir, kapasitas CC, jumlah kursi).
     */
    public abstract String getDetailSpesifik();

    /** @return kode jenis kendaraan yang disimpan di database: MOBIL / MOTOR / BUS. */
    public abstract String getJenis();

    /* ==================== OVERLOADING ==================== */

    /**
     * Overloading {@link #hitungSewa(int)} dengan tambahan diskon persentase.
     *
     * @param hari lama sewa dalam hari
     * @param diskonPersen diskon 0 - 100
     * @return total biaya setelah diskon
     */
    public double hitungSewa(int hari, double diskonPersen) {
        if (diskonPersen < 0 || diskonPersen > 100) {
            throw new InputTidakValidException("Diskon",
                    "Diskon harus berada di antara 0% sampai 100%.");
        }
        double subtotal = hitungSewa(hari);
        double potongan = subtotal * (diskonPersen / 100.0);
        return subtotal - potongan;
    }

    /* ==================== IMPLEMENTASI DapatDisewa ==================== */

    @Override
    public void prosesSewa() {
        if (!tersedia) {
            throw new KendaraanTidakTersediaException(kodeKendaraan,
                    "Kendaraan " + kodeKendaraan + " (" + merk + ") sedang tidak tersedia "
                            + "karena masih disewa. Silakan pilih kendaraan lain.");
        }
        this.tersedia = false;
    }

    @Override
    public void prosesKembali() {
        this.tersedia = true;
    }

    @Override
    public boolean isTersedia() {
        return tersedia;
    }

    /* ==================== GETTER & SETTER ==================== */

    public String getKodeKendaraan() {
        return kodeKendaraan;
    }

    public void setKodeKendaraan(String kodeKendaraan) {
        if (kodeKendaraan == null || kodeKendaraan.trim().isEmpty()) {
            throw new InputTidakValidException("Kode Kendaraan",
                    "Kode kendaraan wajib diisi dan tidak boleh kosong.");
        }
        String kode = kodeKendaraan.trim().toUpperCase();
        if (!kode.matches("[A-Z0-9\\-]{3,10}")) {
            throw new InputTidakValidException("Kode Kendaraan",
                    "Kode kendaraan harus 3-10 karakter dan hanya boleh berisi huruf, "
                            + "angka, atau tanda hubung. Contoh: MBL-001.");
        }
        this.kodeKendaraan = kode;
    }

    public String getMerk() {
        return merk;
    }

    public void setMerk(String merk) {
        if (merk == null || merk.trim().isEmpty()) {
            throw new InputTidakValidException("Merk",
                    "Merk kendaraan wajib diisi dan tidak boleh kosong.");
        }
        String nilai = merk.trim();
        if (nilai.length() < 2) {
            throw new InputTidakValidException("Merk",
                    "Merk kendaraan minimal 2 karakter.");
        }
        if (nilai.length() > 60) {
            throw new InputTidakValidException("Merk",
                    "Merk kendaraan maksimal 60 karakter.");
        }
        this.merk = nilai;
    }

    public String getPlatNomor() {
        return platNomor;
    }

    public void setPlatNomor(String platNomor) {
        if (platNomor == null || platNomor.trim().isEmpty()) {
            throw new InputTidakValidException("Plat Nomor",
                    "Plat nomor wajib diisi dan tidak boleh kosong.");
        }
        String plat = platNomor.trim().toUpperCase();
        if (!plat.matches("[A-Z0-9 ]{3,15}")) {
            throw new InputTidakValidException("Plat Nomor",
                    "Plat nomor hanya boleh berisi huruf, angka, dan spasi "
                            + "(3-15 karakter). Contoh: B 1234 XYZ.");
        }
        this.platNomor = plat;
    }

    public double getTarifHarian() {
        return tarifHarian;
    }

    public void setTarifHarian(double tarifHarian) {
        if (Double.isNaN(tarifHarian) || Double.isInfinite(tarifHarian)) {
            throw new InputTidakValidException("Tarif Harian",
                    "Tarif harian harus berupa angka yang valid.");
        }
        if (tarifHarian <= 0) {
            throw new InputTidakValidException("Tarif Harian",
                    "Tarif harian harus lebih besar dari 0 (tidak boleh nol atau negatif).");
        }
        if (tarifHarian > 9_999_999_999.99) {
            throw new InputTidakValidException("Tarif Harian",
                    "Tarif harian melebihi batas maksimum kolom database.");
        }
        this.tarifHarian = tarifHarian;
    }

    public void setTersedia(boolean tersedia) {
        this.tersedia = tersedia;
    }

    /* ==================== OBJECT METHOD ==================== */

    @Override
    public String toString() {
        return kodeKendaraan + " - " + merk + " (" + platNomor + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(kodeKendaraan);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Kendaraan lain = (Kendaraan) obj;
        return Objects.equals(kodeKendaraan, lain.kodeKendaraan);
    }
}
