package com.rentalkendaraan.model;

import com.rentalkendaraan.exception.InputTidakValidException;

/**
 * Subclass {@link Kendaraan} untuk motor.
 *
 * <p>Aturan biaya: tarif harian dikali lama sewa, dengan diskon 10%
 * apabila lama sewa minimal 3 hari.</p>
 */
public class Motor extends Kendaraan {

    /** Besar diskon sewa panjang (dalam persen). */
    public static final double DISKON_SEWA_PANJANG = 10.0;

    /** Lama sewa minimal agar diskon berlaku. */
    public static final int MINIMAL_HARI_DISKON = 3;

    private int kapasitasCC;

    public Motor(String kodeKendaraan, String merk, String platNomor,
                 double tarifHarian, boolean tersedia, int kapasitasCC) {
        super(kodeKendaraan, merk, platNomor, tarifHarian, tersedia);
        setKapasitasCC(kapasitasCC);
    }

    /**
     * Overriding: tarif * hari, diskon 10% bila lama sewa >= 3 hari.
     */
    @Override
    public double hitungSewa(int hari) {
        if (hari <= 0) {
            throw new InputTidakValidException("Lama Sewa",
                    "Lama sewa motor harus lebih besar dari 0 hari.");
        }
        double total = getTarifHarian() * hari;
        if (hari >= MINIMAL_HARI_DISKON) {
            total -= total * (DISKON_SEWA_PANJANG / 100.0);
        }
        return total;
    }

    @Override
    public String getDetailSpesifik() {
        return "Motor " + kapasitasCC + " CC (diskon 10% min. "
                + MINIMAL_HARI_DISKON + " hari)";
    }

    @Override
    public String getJenis() {
        return "MOTOR";
    }

    public int getKapasitasCC() {
        return kapasitasCC;
    }

    public void setKapasitasCC(int kapasitasCC) {
        if (kapasitasCC <= 0) {
            throw new InputTidakValidException("Kapasitas CC",
                    "Kapasitas mesin harus lebih besar dari 0 CC.");
        }
        if (kapasitasCC > 5000) {
            throw new InputTidakValidException("Kapasitas CC",
                    "Kapasitas mesin tidak masuk akal (maksimal 5000 CC).");
        }
        this.kapasitasCC = kapasitasCC;
    }
}
