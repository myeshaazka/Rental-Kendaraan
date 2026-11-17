package com.rentalkendaraan.model;

import com.rentalkendaraan.exception.InputTidakValidException;

/**
 * Model data pelanggan (penyewa) yang menyewa kendaraan.
 */
public class Penyewa {

    private int idPenyewa;
    private String nama;
    private String nomorKtp;
    private String nomorTelepon;
    private String alamat;

    /** Constructor untuk penyewa baru (belum memiliki ID dari database). */
    public Penyewa(String nama, String nomorKtp, String nomorTelepon, String alamat) {
        this.idPenyewa = 0;
        setNama(nama);
        setNomorKtp(nomorKtp);
        setNomorTelepon(nomorTelepon);
        setAlamat(alamat);
    }

    /** Constructor untuk data yang dibaca dari database. */
    public Penyewa(int idPenyewa, String nama, String nomorKtp,
                   String nomorTelepon, String alamat) {
        setIdPenyewa(idPenyewa);
        setNama(nama);
        setNomorKtp(nomorKtp);
        setNomorTelepon(nomorTelepon);
        setAlamat(alamat);
    }

    public int getIdPenyewa() {
        return idPenyewa;
    }

    public void setIdPenyewa(int idPenyewa) {
        if (idPenyewa < 0) {
            throw new InputTidakValidException(
                    "ID Penyewa", "ID penyewa tidak boleh negatif.");
        }
        this.idPenyewa = idPenyewa;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        if (nama == null || nama.trim().isEmpty()) {
            throw new InputTidakValidException(
                    "Nama Penyewa", "Nama penyewa wajib diisi dan tidak boleh kosong.");
        }
        String nilai = nama.trim();
        if (nilai.length() < 3) {
            throw new InputTidakValidException(
                    "Nama Penyewa", "Nama penyewa minimal 3 karakter.");
        }
        if (nilai.length() > 80) {
            throw new InputTidakValidException(
                    "Nama Penyewa", "Nama penyewa maksimal 80 karakter.");
        }
        this.nama = nilai;
    }

    public String getNomorKtp() {
        return nomorKtp;
    }

    public void setNomorKtp(String nomorKtp) {
        if (nomorKtp == null || nomorKtp.trim().isEmpty()) {
            throw new InputTidakValidException(
                    "Nomor KTP", "Nomor KTP wajib diisi dan tidak boleh kosong.");
        }
        String ktp = nomorKtp.trim();
        if (!ktp.matches("\\d{16}")) {
            throw new InputTidakValidException(
                    "Nomor KTP", "Nomor KTP harus terdiri dari tepat 16 digit angka.");
        }
        this.nomorKtp = ktp;
    }

    public String getNomorTelepon() {
        return nomorTelepon;
    }

    public void setNomorTelepon(String nomorTelepon) {
        if (nomorTelepon == null || nomorTelepon.trim().isEmpty()) {
            throw new InputTidakValidException(
                    "Nomor Telepon", "Nomor telepon wajib diisi dan tidak boleh kosong.");
        }
        String telepon = nomorTelepon.trim();
        if (!telepon.matches("[0-9+\\- ]{8,20}")) {
            throw new InputTidakValidException(
                    "Nomor Telepon", "Nomor telepon harus 8-20 karakter dan hanya boleh "
                            + "berisi angka, spasi, tanda +, atau tanda hubung.");
        }
        this.nomorTelepon = telepon;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        String nilai = (alamat == null) ? "" : alamat.trim();
        if (nilai.length() > 200) {
            throw new InputTidakValidException("Alamat",
                    "Alamat maksimal 200 karakter.");
        }
        this.alamat = nilai;
    }

    @Override
    public String toString() {
        return nama + " (" + nomorKtp + ")";
    }
}
