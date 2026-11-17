package com.rentalkendaraan.model;

import com.rentalkendaraan.exception.InputTidakValidException;

/**
 * Subclass {@link Kendaraan} untuk mobil.
 *
 * <p>Aturan biaya: tarif harian dikali lama sewa, ditambah biaya sopir
 * per hari apabila opsi {@code denganSopir} aktif.</p>
 */
public class Mobil extends Kendaraan {

    /** Biaya tambahan sopir per hari. */
    public static final double BIAYA_SOPIR_PER_HARI = 150_000.0;

    private boolean denganSopir;

    public Mobil(String kodeKendaraan, String merk, String platNomor,
                 double tarifHarian, boolean tersedia, boolean denganSopir) {
        super(kodeKendaraan, merk, platNomor, tarifHarian, tersedia);
        this.denganSopir = denganSopir;
    }

    /**
     * Overriding: tarif * hari + (biaya sopir * hari bila memakai sopir).
     */
    @Override
    public double hitungSewa(int hari) {
        if (hari <= 0) {
            throw new InputTidakValidException("Lama Sewa",
                    "Lama sewa mobil harus lebih besar dari 0 hari.");
        }
        double total = getTarifHarian() * hari;
        if (denganSopir) {
            total += BIAYA_SOPIR_PER_HARI * hari;
        }
        return total;
    }

    @Override
    public String getDetailSpesifik() {
        if (denganSopir) {
            return "Mobil + sopir (Rp150.000/hari)";
        }
        return "Mobil tanpa sopir (lepas kunci)";
    }

    @Override
    public String getJenis() {
        return "MOBIL";
    }

    public boolean isDenganSopir() {
        return denganSopir;
    }

    public void setDenganSopir(boolean denganSopir) {
        this.denganSopir = denganSopir;
    }
}
