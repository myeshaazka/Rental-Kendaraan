package com.rentalkendaraan.exception;

/**
 * Exception yang dilempar ketika sebuah kendaraan hendak disewa
 * tetapi statusnya sedang tidak tersedia (masih disewa pelanggan lain).
 */
public class KendaraanTidakTersediaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String kodeKendaraan;

    public KendaraanTidakTersediaException(String pesan) {
        super(pesan);
        this.kodeKendaraan = "-";
    }

    public KendaraanTidakTersediaException(String kodeKendaraan, String pesan) {
        super(pesan);
        this.kodeKendaraan = kodeKendaraan;
    }

    public String getKodeKendaraan() {
        return kodeKendaraan;
    }
}
