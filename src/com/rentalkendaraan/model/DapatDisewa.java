package com.rentalkendaraan.model;

/**
 * Interface kontrak untuk objek yang memiliki siklus sewa-menyewa.
 * Diimplementasikan oleh {@link Kendaraan} (dan turunannya) serta
 * {@link TransaksiSewa}.
 */
public interface DapatDisewa {

    /** Menandai objek sebagai sedang disewa. */
    void prosesSewa();

    /** Menandai objek sebagai sudah dikembalikan / kembali tersedia. */
    void prosesKembali();

    /** @return {@code true} bila objek masih dapat disewa / transaksi masih berjalan. */
    boolean isTersedia();
}
