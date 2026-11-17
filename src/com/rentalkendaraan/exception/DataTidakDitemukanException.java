package com.rentalkendaraan.exception;

/**
 * Exception yang dilempar ketika data yang dicari (kendaraan/transaksi)
 * tidak ditemukan di database.
 */
public class DataTidakDitemukanException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataTidakDitemukanException(String pesan) {
        super(pesan);
    }
}
