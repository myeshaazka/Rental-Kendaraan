package com.rentalkendaraan.model;

import com.rentalkendaraan.exception.InputTidakValidException;

/**
 * Subclass {@link Kendaraan} untuk bus.
 *
 * <p>Aturan biaya: tarif harian dikali lama sewa, ditambah biaya tetap
 * kru dan asuransi per transaksi.</p>
 */
public class Bus extends Kendaraan {

    /** Biaya tetap kru + asuransi untuk setiap transaksi bus. */
    public static final double BIAYA_KRU_ASURANSI = 250_000.0;

    private int jumlahKursi;

    public Bus(String kodeKendaraan, String merk, String platNomor,
               double tarifHarian, boolean tersedia, int jumlahKursi) {
        super(kodeKendaraan, merk, platNomor, tarifHarian, tersedia);
        setJumlahKursi(jumlahKursi);
    }

    /**
     * Overriding: tarif * hari + biaya tetap kru/asuransi.
     */
    @Override
    public double hitungSewa(int hari) {
        if (hari <= 0) {
            throw new InputTidakValidException("Lama Sewa",
                    "Lama sewa bus harus lebih besar dari 0 hari.");
        }
        return (getTarifHarian() * hari) + BIAYA_KRU_ASURANSI;
    }

    @Override
    public String getDetailSpesifik() {
        return "Bus " + jumlahKursi + " kursi (+kru & asuransi Rp250.000)";
    }

    @Override
    public String getJenis() {
        return "BUS";
    }

    public int getJumlahKursi() {
        return jumlahKursi;
    }

    public void setJumlahKursi(int jumlahKursi) {
        if (jumlahKursi <= 0) {
            throw new InputTidakValidException("Jumlah Kursi",
                    "Jumlah kursi bus harus lebih besar dari 0.");
        }
        if (jumlahKursi > 100) {
            throw new InputTidakValidException("Jumlah Kursi",
                    "Jumlah kursi bus tidak boleh lebih dari 100.");
        }
        this.jumlahKursi = jumlahKursi;
    }
}
